package com.example.wheresxyz.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(val latitude: Double, val longitude: Double)

private const val EARTH_RADIUS_METERS = 6_371_000.0

fun calculateOffsetLatLng(base: GeoPoint, distanceMeters: Double, bearingDegrees: Double): GeoPoint {
    val angularDistance = distanceMeters / EARTH_RADIUS_METERS
    val bearingRad = Math.toRadians(bearingDegrees)

    val lat1 = Math.toRadians(base.latitude)
    val lon1 = Math.toRadians(base.longitude)

    val lat2 = Math.asin(
        sin(lat1) * cos(angularDistance) +
            cos(lat1) * sin(angularDistance) * cos(bearingRad)
    )

    val lon2 = lon1 + atan2(
        sin(bearingRad) * sin(angularDistance) * cos(lat1),
        cos(angularDistance) - sin(lat1) * sin(lat2)
    )

    return GeoPoint(Math.toDegrees(lat2), Math.toDegrees(lon2))
}

fun calculateDistanceMeters(from: GeoPoint, to: GeoPoint): Int {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val deltaLat = lat2 - lat1
    val deltaLon = Math.toRadians(to.longitude - from.longitude)

    val a = sin(deltaLat / 2).pow(2) +
        cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (EARTH_RADIUS_METERS * c).toInt()
}

fun calculateBearingDegrees(from: GeoPoint, to: GeoPoint): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val deltaLon = Math.toRadians(to.longitude - from.longitude)

    val y = sin(deltaLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
    return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}

fun formatDistanceMeters(distanceMeters: Int): String {
    return if (distanceMeters >= 1000) {
        String.format("%.1f km", distanceMeters / 1000f)
    } else {
        "${distanceMeters}m"
    }
}

fun isLocationStale(updatedAt: Long, nowMillis: Long, maxAgeMillis: Long): Boolean {
    return nowMillis - updatedAt > maxAgeMillis
}
