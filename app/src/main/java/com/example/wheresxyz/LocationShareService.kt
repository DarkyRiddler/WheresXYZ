package com.example.wheresxyz

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wheresxyz.data.model.SharedLocation
import com.example.wheresxyz.data.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationShareService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    private var currentEventId: String? = null
    private var currentUserKey: String? = null
    private var currentDisplayName: String? = null
    private var currentAvatar: String? = null

    companion object {
        private const val CHANNEL_ID = "location_share_channel"
        private const val NOTIFICATION_ID = 4567

        const val ACTION_START = "com.example.wheresxyz.action.START"
        const val ACTION_STOP = "com.example.wheresxyz.action.STOP"

        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_USER_KEY = "extra_user_key"
        const val EXTRA_DISPLAY_NAME = "extra_display_name"
        const val EXTRA_AVATAR = "extra_avatar"

        // Expose reactive status flows so ViewModels can synchronize their UI with the active service
        val activeEventId = MutableStateFlow<String?>(null)
        val activeUser = MutableStateFlow<com.example.wheresxyz.data.model.User?>(null)
        val isSharingActive = MutableStateFlow(false)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_START -> {
                val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
                val userKey = intent.getStringExtra(EXTRA_USER_KEY)
                val displayName = intent.getStringExtra(EXTRA_DISPLAY_NAME)
                val avatar = intent.getStringExtra(EXTRA_AVATAR)

                if (eventId != null && userKey != null && displayName != null) {
                    currentEventId = eventId
                    currentUserKey = userKey
                    currentDisplayName = displayName
                    currentAvatar = avatar ?: "👤"

                    val reconstructedUser = com.example.wheresxyz.data.model.User(
                        id = userKey,
                        name = displayName.split(" ").firstOrNull() ?: "",
                        lastname = displayName.split(" ").getOrNull(1) ?: "",
                        email = userKey, // userKey is email in lowercase
                        userPhoto = avatar
                    )

                    activeEventId.value = eventId
                    activeUser.value = reconstructedUser
                    isSharingActive.value = true

                    startForegroundServiceCompat(displayName)
                    startLocationUpdates()
                } else {
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopSharing()
            }
            else -> stopSelf()
        }

        return START_STICKY
    }

    private fun startForegroundServiceCompat(displayName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Where's XYZ")
            .setContentText("Udostępniasz swoją lokalizację ($displayName)")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                publishLocation(location.latitude, location.longitude)
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        try {
            // Request location updates every 30 seconds or 5 meters move
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                30000L,
                5f,
                locationListener!!
            )
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                30000L,
                5f,
                locationListener!!
            )

            // Trigger immediate last known location publish
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = if (lastGps != null && lastNet != null) {
                if (lastGps.time > lastNet.time) lastGps else lastNet
            } else lastGps ?: lastNet

            best?.let {
                publishLocation(it.latitude, it.longitude)
            }
        } catch (e: SecurityException) {
            // no permission
        }
    }

    private fun publishLocation(latitude: Double, longitude: Double) {
        val eventId = currentEventId ?: return
        val userKey = currentUserKey ?: return
        val displayName = currentDisplayName ?: return
        val avatar = currentAvatar ?: "👤"

        serviceScope.launch {
            locationRepository.publishLocation(
                eventId,
                SharedLocation(
                    userKey = userKey,
                    displayName = displayName,
                    avatar = avatar,
                    latitude = latitude,
                    longitude = longitude,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun stopSharing() {
        locationListener?.let {
            try {
                locationManager?.removeUpdates(it)
            } catch (e: Exception) {
                // ignore
            }
        }
        locationListener = null
        locationManager = null

        val eventId = currentEventId
        val userKey = currentUserKey
        if (eventId != null && userKey != null) {
            serviceScope.launch {
                locationRepository.stopSharing(eventId, userKey)
            }
        }

        activeEventId.value = null
        activeUser.value = null
        isSharingActive.value = false

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        // Reset states just in case
        activeEventId.value = null
        activeUser.value = null
        isSharingActive.value = false
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Udostępnianie lokalizacji",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
