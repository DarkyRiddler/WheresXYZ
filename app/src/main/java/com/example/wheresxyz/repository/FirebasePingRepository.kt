package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.PingDto
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePingRepository @Inject constructor(
    private val database: FirebaseDatabase
) : PingRepository {
    private val pingsRef = database.getReference("pings")

    override suspend fun getUserPings(userId: String): Result<List<PingDto>> = try {
        val snapshot = pingsRef.child(userId).get().await()
        val pings = snapshot.children.mapNotNull { it.getValue(PingDto::class.java) }
        Result.success(pings)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendPing(ping: PingDto): Result<Unit> = try {
        // In Firebase, we might want to store pings under the recipient user's ID
        val newPingRef = pingsRef.child(ping.userId.toString()).push()
        newPingRef.setValue(ping).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deletePing(pingId: String): Result<Unit> = try {
        // This assumes pingId is a full path or we have a way to find it.
        // For simplicity, let's assume we pass the path or handle it differently.
        // Real implementation would depend on how pings are indexed.
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
