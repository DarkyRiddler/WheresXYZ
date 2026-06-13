package com.example.wheresxyz.data.model

import com.example.wheresxyz.util.isLocationStale

data class SharedLocation(
    val userKey: String = "",
    val displayName: String = "",
    val avatar: String = "👤",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val updatedAt: Long = 0L
) {
    fun isStale(nowMillis: Long = System.currentTimeMillis(), maxAgeMillis: Long = STALE_THRESHOLD_MS): Boolean {
        return isLocationStale(updatedAt, nowMillis, maxAgeMillis)
    }

    companion object {
        const val STALE_THRESHOLD_MS = 120_000L
    }
}

fun User.locationKey(): String = email.lowercase().trim()

fun User.displayLabel(): String = "$name $lastname".trim()
