package com.example.wheresxyz.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.wheresxyz.LocationShareService
import com.example.wheresxyz.data.model.Event
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.model.displayLabel
import com.example.wheresxyz.data.model.locationKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleEvents(events: List<Event>, currentUser: User) {
        val now = System.currentTimeMillis()
        events.forEach { event ->
            if (event.endDate < now) {
                cancelAlarms(event)
            } else {
                scheduleAlarms(event, currentUser)
            }
        }
    }

    fun scheduleAlarms(event: Event, currentUser: User) {
        val now = System.currentTimeMillis()

        // 1. Start Alarm
        if (event.startDate > now) {
            val startIntent = Intent(context, LocationShareService::class.java).apply {
                action = LocationShareService.ACTION_START
                putExtra(LocationShareService.EXTRA_EVENT_ID, event.id)
                putExtra(LocationShareService.EXTRA_USER_KEY, currentUser.locationKey())
                putExtra(LocationShareService.EXTRA_DISPLAY_NAME, currentUser.displayLabel())
                putExtra(LocationShareService.EXTRA_AVATAR, currentUser.userPhoto)
            }
            val startPendingIntent = PendingIntent.getService(
                context,
                getStartRequestCode(event.id),
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setAlarm(event.startDate, startPendingIntent)
        } else if (event.isActive) {
            // If the event is already active, start the location sharing foreground service immediately!
            val startIntent = Intent(context, LocationShareService::class.java).apply {
                action = LocationShareService.ACTION_START
                putExtra(LocationShareService.EXTRA_EVENT_ID, event.id)
                putExtra(LocationShareService.EXTRA_USER_KEY, currentUser.locationKey())
                putExtra(LocationShareService.EXTRA_DISPLAY_NAME, currentUser.displayLabel())
                putExtra(LocationShareService.EXTRA_AVATAR, currentUser.userPhoto)
            }
            try {
                androidx.core.content.ContextCompat.startForegroundService(context, startIntent)
            } catch (e: Exception) {
                // ignore start foreground service failure due to background restrictions if any
            }
        }

        // 2. Stop Alarm (triggers ACTION_STOP at the event's end time)
        if (event.endDate > now) {
            val stopIntent = Intent(context, LocationShareService::class.java).apply {
                action = LocationShareService.ACTION_STOP
            }
            val stopPendingIntent = PendingIntent.getService(
                context,
                getStopRequestCode(event.id),
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setAlarm(event.endDate, stopPendingIntent)
        }
    }

    fun cancelAlarms(event: Event) {
        val startIntent = Intent(context, LocationShareService::class.java).apply {
            action = LocationShareService.ACTION_START
        }
        val startPendingIntent = PendingIntent.getService(
            context,
            getStartRequestCode(event.id),
            startIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (startPendingIntent != null) {
            alarmManager.cancel(startPendingIntent)
            startPendingIntent.cancel()
        }

        val stopIntent = Intent(context, LocationShareService::class.java).apply {
            action = LocationShareService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            getStopRequestCode(event.id),
            stopIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (stopPendingIntent != null) {
            alarmManager.cancel(stopPendingIntent)
            stopPendingIntent.cancel()
        }
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun getStartRequestCode(eventId: String): Int {
        return eventId.hashCode()
    }

    private fun getStopRequestCode(eventId: String): Int {
        return eventId.hashCode() xor 0x0F0F0F0F
    }
}
