package com.example.wheresxyz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheresxyz.data.model.SharedLocation
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.model.displayLabel
import com.example.wheresxyz.data.model.locationKey
import com.example.wheresxyz.data.repository.LocationRepository
import com.example.wheresxyz.util.GeoPoint
import com.example.wheresxyz.util.calculateDistanceMeters
import dagger.hilt.android.lifecycle.HiltViewModel
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
    object Fallback : LocationSyncState
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
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<LocationSyncState>(LocationSyncState.Idle)
    val syncState: StateFlow<LocationSyncState> = _syncState.asStateFlow()

    private val _remoteParticipants = MutableStateFlow<List<RemoteParticipant>>(emptyList())
    val remoteParticipants: StateFlow<List<RemoteParticipant>> = _remoteParticipants.asStateFlow()

    private var observeJob: Job? = null
    private var activeEventId: String? = null
    private var activeUser: User? = null
    private var lastMyLocation: GeoPoint? = null

    fun startSharing(eventId: String, user: User) {
        if (activeEventId == eventId && activeUser?.locationKey() == user.locationKey()) return

        stopSharingInternal(removeFromServer = true)

        activeEventId = eventId
        activeUser = user
        _syncState.value = LocationSyncState.Connecting

        viewModelScope.launch {
            locationRepository.ensureSession()
                .onSuccess {
                    _syncState.value = LocationSyncState.Active
                    observeJob = viewModelScope.launch {
                        locationRepository.observeLocations(eventId).collect { locations ->
                            updateRemoteParticipants(locations, user)
                        }
                    }
                    lastMyLocation?.let { (lat, lng) ->
                        publishLocation(user, lat, lng)
                    }
                }
                .onFailure {
                    _syncState.value = LocationSyncState.Fallback
                    _remoteParticipants.value = emptyList()
                }
        }
    }

    fun updateMyLocation(latitude: Double, longitude: Double) {
        lastMyLocation = GeoPoint(latitude, longitude)
        val user = activeUser ?: return

        updateRemoteParticipantsFromCache(latitude, longitude)

        if (_syncState.value != LocationSyncState.Active) return

        viewModelScope.launch {
            publishLocation(user, latitude, longitude)
        }
    }

    private suspend fun publishLocation(user: User, latitude: Double, longitude: Double) {
        val eventId = activeEventId ?: return
        locationRepository.publishLocation(
            eventId,
            SharedLocation(
                userKey = user.locationKey(),
                displayName = user.displayLabel(),
                avatar = user.userPhoto ?: "👤",
                latitude = latitude,
                longitude = longitude,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun stopSharing() {
        stopSharingInternal(removeFromServer = true)
    }

    private fun stopSharingInternal(removeFromServer: Boolean) {
        observeJob?.cancel()
        observeJob = null

        val eventId = activeEventId
        val userKey = activeUser?.locationKey()

        if (removeFromServer && eventId != null && userKey != null) {
            viewModelScope.launch {
                locationRepository.stopSharing(eventId, userKey)
            }
        }

        activeEventId = null
        activeUser = null
        lastMyLocation = null
        _remoteParticipants.value = emptyList()
        _syncState.value = LocationSyncState.Idle
    }

    private fun updateRemoteParticipants(locations: List<SharedLocation>, currentUser: User) {
        val myLocation = lastMyLocation
        val now = System.currentTimeMillis()

        _remoteParticipants.value = locations
            .filter { it.userKey != currentUser.locationKey() && !it.isStale(now) }
            .map { location ->
                val distance = myLocation?.let {
                    calculateDistanceMeters(it, GeoPoint(location.latitude, location.longitude))
                } ?: 0
                RemoteParticipant(
                    userKey = location.userKey,
                    displayName = location.displayName,
                    avatar = location.avatar,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    distanceMeters = distance
                )
            }
            .sortedBy { it.distanceMeters }
    }

    private fun updateRemoteParticipantsFromCache(latitude: Double, longitude: Double) {
        val current = _remoteParticipants.value
        if (current.isEmpty()) return

        val myPoint = GeoPoint(latitude, longitude)
        _remoteParticipants.value = current.map { participant ->
            participant.copy(
                distanceMeters = calculateDistanceMeters(
                    myPoint,
                    GeoPoint(participant.latitude, participant.longitude)
                )
            )
        }.sortedBy { it.distanceMeters }
    }

    override fun onCleared() {
        stopSharingInternal(removeFromServer = true)
        super.onCleared()
    }
}
