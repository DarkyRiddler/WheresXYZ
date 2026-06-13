package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.ApiService
import com.example.wheresxyz.data.remote.model.PingDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PingRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUserPings(userId: Int) = try {
        val response = apiService.getUserPings(userId)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Failed to fetch pings"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sendPing(ping: PingDto) = try {
        // Logika biznesowa: sprawdzenie limitu 5 pingów odbywa się zazwyczaj po stronie backendu,
        // ale w Repository można dodać wstępną weryfikację.
        val response = apiService.sendPing(ping)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Limit reached or server error"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePing(pingId: Int) = apiService.deletePing(pingId)
}
