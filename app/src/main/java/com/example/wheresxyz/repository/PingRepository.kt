package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.PingDto

interface PingRepository {
    suspend fun getUserPings(userId: String): Result<List<PingDto>>
    suspend fun sendPing(ping: PingDto): Result<Unit>
    suspend fun deletePing(pingId: String): Result<Unit>
}
