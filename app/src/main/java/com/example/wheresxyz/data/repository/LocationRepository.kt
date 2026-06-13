package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.model.SharedLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun ensureSession(): Result<Unit>
    suspend fun publishLocation(eventId: String, location: SharedLocation): Result<Unit>
    fun observeLocations(eventId: String): Flow<List<SharedLocation>>
    suspend fun stopSharing(eventId: String, userKey: String): Result<Unit>
}
