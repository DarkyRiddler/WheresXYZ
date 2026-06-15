package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.model.SharedLocation
import com.example.wheresxyz.util.sanitizeFirebaseKey
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseLocationRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val sessionManager: FirebaseSessionManager
) : LocationRepository {

    private val locationsRef get() = database.getReference("live_locations")

    override suspend fun ensureSession(): Result<Unit> = sessionManager.ensureAnonymousAuth()

    override suspend fun publishLocation(eventId: String, location: SharedLocation): Result<Unit> {
        return try {
            ensureSession().getOrThrow()
            val ref = locationsRef.child(eventId).child(sanitizeFirebaseKey(location.userKey))
            ref.setValue(location.toFirebaseMap()).await()
            ref.onDisconnect().removeValue()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeLocations(eventId: String): Flow<List<SharedLocation>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = snapshot.children.mapNotNull { child ->
                    child.getValue(SharedLocation::class.java)
                }
                trySend(locations)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = locationsRef.child(eventId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun stopSharing(eventId: String, userKey: String): Result<Unit> {
        return try {
            locationsRef.child(eventId).child(sanitizeFirebaseKey(userKey)).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPing(targetEmail: String, senderName: String): Result<Unit> {
        return try {
            ensureSession().getOrThrow()
            val ref = database.getReference("pings").push()
            val pingData = mapOf(
                "senderName" to senderName,
                "targetEmail" to targetEmail,
                "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
            )
            ref.setValue(pingData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun SharedLocation.toFirebaseMap(): Map<String, Any> = mapOf(
        "userKey" to userKey,
        "displayName" to displayName,
        "avatar" to avatar,
        "latitude" to latitude,
        "longitude" to longitude,
        "updatedAt" to updatedAt
    )
}
