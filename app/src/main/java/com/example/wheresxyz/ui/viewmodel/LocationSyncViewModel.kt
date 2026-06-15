package com.example.wheresxyz.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheresxyz.LocationShareService
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.model.displayLabel
import com.example.wheresxyz.data.model.locationKey
import com.example.wheresxyz.data.repository.LocationRepository
import com.example.wheresxyz.util.GeoPoint
import com.example.wheresxyz.util.buildRemoteParticipants
import com.example.wheresxyz.util.recalculateParticipantDistances
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LocationSyncState {
    object Idle : LocationSyncState
    object Connecting : LocationSyncState
    object Active : LocationSyncState
}

data class RemoteParticipant(
    val userKey: String,
    val displayName: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int
)

@HiltViewModel
class LocationSyncViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _syncState = MutableStateFlow<LocationSyncState>(LocationSyncState.Idle)
    val syncState: StateFlow<LocationSyncState> = _syncState.asStateFlow()

    private val _remoteParticipants = MutableStateFlow<List<RemoteParticipant>>(emptyList())
    val remoteParticipants: StateFlow<List<RemoteParticipant>> = _remoteParticipants.asStateFlow()

    private var observeJob: Job? = null
    private var lastMyLocation: GeoPoint? = null

    init {
        // Automatically sync the UI state and start observing remote positions if sharing is active in the background
        viewModelScope.launch {
            LocationShareService.activeEventId.collect { eventId ->
                if (eventId != null) {
                    val user = LocationShareService.activeUser.value
                    if (user != null) {
                        _syncState.value = LocationSyncState.Active
                        startObservingRemote(eventId, user)
                    }
                } else {
                    _syncState.value = LocationSyncState.Idle
                    _remoteParticipants.value = emptyList()
                    observeJob?.cancel()
                    observeJob = null
                }
            }
        }
    }

    private fun startObservingRemote(eventId: String, user: User) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            while (true) {
                try {
                    locationRepository.observeLocations(eventId).collect { locations ->
                        _syncState.value = LocationSyncState.Active
                        updateRemoteParticipants(locations, user)
                    }
                } catch (e: Exception) {
                    _syncState.value = LocationSyncState.Connecting
                    _remoteParticipants.value = emptyList()
                    kotlinx.coroutines.delay(5000L) // Wait 5s before retrying database collection
                }
            }
        }
    }

    fun startSharing(eventId: String, user: User) {
        if (LocationShareService.activeEventId.value == eventId) return

        _syncState.value = LocationSyncState.Connecting
        val intent = Intent(context, LocationShareService::class.java).apply {
            action = LocationShareService.ACTION_START
            putExtra(LocationShareService.EXTRA_EVENT_ID, eventId)
            putExtra(LocationShareService.EXTRA_USER_KEY, user.locationKey())
            putExtra(LocationShareService.EXTRA_DISPLAY_NAME, user.displayLabel())
            putExtra(LocationShareService.EXTRA_AVATAR, user.userPhoto)
        }
        androidx.core.content.ContextCompat.startForegroundService(context, intent)
    }

    fun stopSharing() {
        val intent = Intent(context, LocationShareService::class.java).apply {
            action = LocationShareService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun updateMyLocation(latitude: Double, longitude: Double) {
        lastMyLocation = GeoPoint(latitude, longitude)
        updateRemoteParticipantsFromCache(latitude, longitude)
    }

    private fun updateRemoteParticipants(locations: List<com.example.wheresxyz.data.model.SharedLocation>, currentUser: User) {
        _remoteParticipants.value = buildRemoteParticipants(
            locations = locations,
            currentUser = currentUser,
            myLocation = lastMyLocation
        )
    }

    private fun updateRemoteParticipantsFromCache(latitude: Double, longitude: Double) {
        val current = _remoteParticipants.value
        if (current.isEmpty()) return

        _remoteParticipants.value = recalculateParticipantDistances(current, GeoPoint(latitude, longitude))
    }

    fun sendPing(targetEmail: String, senderEmail: String, senderName: String) {
        viewModelScope.launch {
            locationRepository.sendPing(targetEmail, senderEmail, senderName)
        }
    }

    fun sendGroupPing(targetGroupId: String, senderEmail: String, senderName: String) {
        viewModelScope.launch {
            locationRepository.sendGroupPing(targetGroupId, senderEmail, senderName)
        }
    }
}
