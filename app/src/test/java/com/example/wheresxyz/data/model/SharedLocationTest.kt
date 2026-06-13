package com.example.wheresxyz.data.model

import com.example.wheresxyz.util.isLocationStale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedLocationTest {

    @Test
    fun isStale_returnsTrueForOldTimestamp() {
        val location = SharedLocation(
            userKey = "user@example.com",
            displayName = "User",
            latitude = 52.0,
            longitude = 21.0,
            updatedAt = 1_000L
        )

        assertTrue(location.isStale(nowMillis = 200_000L, maxAgeMillis = SharedLocation.STALE_THRESHOLD_MS))
    }

    @Test
    fun isStale_returnsFalseForRecentTimestamp() {
        val now = 500_000L
        val location = SharedLocation(
            userKey = "user@example.com",
            displayName = "User",
            latitude = 52.0,
            longitude = 21.0,
            updatedAt = now - 30_000L
        )

        assertFalse(location.isStale(nowMillis = now, maxAgeMillis = SharedLocation.STALE_THRESHOLD_MS))
    }

    @Test
    fun userLocationKey_isStableForSameEmail() {
        val user = User(1, 1234, "Jan", "Kowalski", "Jan@Example.COM")
        assertEquals("jan@example.com", user.locationKey())
    }
}
