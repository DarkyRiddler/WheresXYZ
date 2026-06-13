package com.example.wheresxyz.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationMathTest {

    @Test
    fun calculateOffsetLatLng_returnsSamePointForZeroDistance() {
        val base = GeoPoint(52.2297, 21.0122)
        val result = calculateOffsetLatLng(base, 0.0, 90.0)
        assertEquals(base.latitude, result.latitude, 0.0001)
        assertEquals(base.longitude, result.longitude, 0.0001)
    }

    @Test
    fun calculateDistanceMeters_returnsZeroForSamePoint() {
        val point = GeoPoint(52.2297, 21.0122)
        assertEquals(0, calculateDistanceMeters(point, point))
    }

    @Test
    fun calculateDistanceMeters_isSymmetric() {
        val warsaw = GeoPoint(52.2297, 21.0122)
        val krakow = GeoPoint(50.0647, 19.9450)
        val forward = calculateDistanceMeters(warsaw, krakow)
        val backward = calculateDistanceMeters(krakow, warsaw)
        assertEquals(forward, backward)
        assertTrue(forward in 250_000..260_000)
    }

    @Test
    fun calculateBearingDegrees_pointsNorthWhenDestinationIsNorth() {
        val base = GeoPoint(52.0, 21.0)
        val north = GeoPoint(53.0, 21.0)
        val bearing = calculateBearingDegrees(base, north)
        assertEquals(0.0, bearing, 1.0)
    }

    @Test
    fun formatDistanceMeters_formatsShortAndLongDistances() {
        assertEquals("150m", formatDistanceMeters(150))
        assertEquals("1.5 km", formatDistanceMeters(1500))
    }

    @Test
    fun isLocationStale_detectsExpiredEntries() {
        val now = 1_000_000L
        assertTrue(isLocationStale(updatedAt = 700_000L, nowMillis = now, maxAgeMillis = 120_000L))
        assertFalse(isLocationStale(updatedAt = 950_000L, nowMillis = now, maxAgeMillis = 120_000L))
    }
}
