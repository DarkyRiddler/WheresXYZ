package com.example.wheresxyz.util

import com.example.wheresxyz.data.model.SharedLocation
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.model.locationKey
import com.example.wheresxyz.ui.viewmodel.RemoteParticipant

fun buildRemoteParticipants(
    locations: List<SharedLocation>,
    currentUser: User,
    myLocation: GeoPoint?,
    nowMillis: Long = System.currentTimeMillis()
): List<RemoteParticipant> {
    return locations
        .filter { it.userKey != currentUser.locationKey() && !it.isStale(nowMillis) }
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

fun recalculateParticipantDistances(
    participants: List<RemoteParticipant>,
    myLocation: GeoPoint
): List<RemoteParticipant> {
    return participants
        .map { participant ->
            participant.copy(
                distanceMeters = calculateDistanceMeters(
                    myLocation,
                    GeoPoint(participant.latitude, participant.longitude)
                )
            )
        }
        .sortedBy { it.distanceMeters }
}
