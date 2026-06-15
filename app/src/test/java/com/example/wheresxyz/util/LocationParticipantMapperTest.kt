package com.example.wheresxyz.util

import com.example.wheresxyz.data.model.SharedLocation
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.util.GeoPoint
import com.example.wheresxyz.ui.viewmodel.RemoteParticipant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationParticipantMapperTest {

    private val currentUser = User(
        id = "uid_1",
        userCode = 1234,
        name = "Jan",
        lastname = "Kowalski",
        email = "jan@example.com"
    )

    @Test
    fun buildRemoteParticipants_excludesCurrentUserAndStaleLocations() {
        val now = 1_000_000L
        val locations = listOf(
            SharedLocation(
                userKey = "jan@example.com",
                displayName = "Jan Kowalski",
                latitude = 52.0,
                longitude = 21.0,
                updatedAt = now
            ),
            SharedLocation(
                userKey = "anna@example.com",
                displayName = "Anna Nowak",
                latitude = 52.001,
                longitude = 21.001,
                updatedAt = now
            ),
            SharedLocation(
                userKey = "old@example.com",
                displayName = "Old User",
                latitude = 52.002,
                longitude = 21.002,
                updatedAt = now - SharedLocation.STALE_THRESHOLD_MS - 1
            )
        )

        val participants = buildRemoteParticipants(
            locations = locations,
            currentUser = currentUser,
            myLocation = GeoPoint(52.0, 21.0),
            nowMillis = now
        )

        assertEquals(1, participants.size)
        assertEquals("Anna Nowak", participants.first().displayName)
        assertTrue(participants.first().distanceMeters > 0)
    }

    @Test
    fun buildRemoteParticipants_sortsByDistanceAscending() {
        val now = 1_000_000L
        val locations = listOf(
            SharedLocation(
                userKey = "far@example.com",
                displayName = "Far",
                latitude = 52.01,
                longitude = 21.01,
                updatedAt = now
            ),
            SharedLocation(
                userKey = "near@example.com",
                displayName = "Near",
                latitude = 52.0001,
                longitude = 21.0001,
                updatedAt = now
            )
        )

        val participants = buildRemoteParticipants(
            locations = locations,
            currentUser = currentUser,
            myLocation = GeoPoint(52.0, 21.0),
            nowMillis = now
        )

        assertEquals(listOf("Near", "Far"), participants.map { it.displayName })
        assertTrue(participants.first().distanceMeters < participants.last().distanceMeters)
    }

    @Test
    fun recalculateParticipantDistances_updatesDistancesFromNewPosition() {
        val participants = listOf(
            RemoteParticipant(
                userKey = "near@example.com",
                displayName = "Near",
                avatar = "👤",
                latitude = 52.0001,
                longitude = 21.0001,
                distanceMeters = 999
            ),
            RemoteParticipant(
                userKey = "far@example.com",
                displayName = "Far",
                avatar = "👤",
                latitude = 52.01,
                longitude = 21.01,
                distanceMeters = 1
            )
        )

        val updated = recalculateParticipantDistances(participants, GeoPoint(52.0, 21.0))

        assertEquals("Near", updated.first().displayName)
        assertTrue(updated.first().distanceMeters < updated.last().distanceMeters)
    }
}
