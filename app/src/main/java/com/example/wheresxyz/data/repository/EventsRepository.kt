package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.model.Event
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class EventsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val eventsCollection = firestore.collection("Events")

    private fun parseEvent(doc: DocumentSnapshot): Event? {
        return try {
            val id = doc.id
            val title = doc.getString("title") ?: ""
            val description = doc.getString("description") ?: ""
            val startDate = doc.getLong("startDate") ?: 0L
            val endDate = doc.getLong("endDate") ?: 0L
            val groupId = doc.getString("groupId") ?: ""
            val groupName = doc.getString("groupName") ?: ""
            val createdBy = doc.getString("createdBy") ?: ""
            val startLatitude = doc.getDouble("startLatitude") ?: 0.0
            val startLongitude = doc.getDouble("startLongitude") ?: 0.0
            val allowedDistance = doc.getDouble("allowedDistance") ?: 0.0
            Event(
                id = id,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                groupId = groupId,
                groupName = groupName,
                createdBy = createdBy,
                startLatitude = startLatitude,
                startLongitude = startLongitude,
                allowedDistance = allowedDistance
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEventsForGroups(groupIds: List<String>): Result<List<Event>> {
        if (groupIds.isEmpty()) return Result.success(emptyList())
        return try {
            val events = if (groupIds.size <= 30) {
                val snapshot = withTimeout(10.seconds) {
                    eventsCollection.whereIn("groupId", groupIds).get().await()
                }
                snapshot.documents.mapNotNull { parseEvent(it) }
            } else {
                groupIds.chunked(30).flatMap { chunk ->
                    val snapshot = withTimeout(10.seconds) {
                        eventsCollection.whereIn("groupId", chunk).get().await()
                    }
                    snapshot.documents.mapNotNull { parseEvent(it) }
                }
            }
            Result.success(events.sortedByDescending { it.startDate })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createEvent(
        title: String,
        description: String,
        startDate: Long,
        endDate: Long,
        groupId: String,
        groupName: String,
        createdBy: String,
        startLatitude: Double,
        startLongitude: Double,
        allowedDistance: Double
    ): Result<Event> {
        return try {
            val docRef = eventsCollection.document()
            val eventData = mapOf(
                "title" to title,
                "description" to description,
                "startDate" to startDate,
                "endDate" to endDate,
                "groupId" to groupId,
                "groupName" to groupName,
                "createdBy" to createdBy,
                "startLatitude" to startLatitude,
                "startLongitude" to startLongitude,
                "allowedDistance" to allowedDistance
            )
            withTimeout(10.seconds) {
                docRef.set(eventData).await()
            }
            val event = Event(
                id = docRef.id,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                groupId = groupId,
                groupName = groupName,
                createdBy = createdBy,
                startLatitude = startLatitude,
                startLongitude = startLongitude,
                allowedDistance = allowedDistance
            )
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            withTimeout(10.seconds) {
                eventsCollection.document(eventId).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
