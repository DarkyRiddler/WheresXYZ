package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.GroupDto
import com.example.wheresxyz.data.remote.model.EventDto
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseGroupRepository @Inject constructor(
    private val database: FirebaseDatabase
) : GroupRepository {
    private val groupsRef = database.getReference("groups")

    override suspend fun getAllGroups(): Result<List<GroupDto>> = try {
        val snapshot = groupsRef.get().await()
        val groups = snapshot.children.mapNotNull { it.getValue(GroupDto::class.java) }
        Result.success(groups)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createGroup(group: GroupDto): Result<Unit> = try {
        groupsRef.child(group.id.toString()).setValue(group).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getGroupEvent(groupId: Int): Result<EventDto?> = try {
        val snapshot = database.getReference("events").child(groupId.toString()).get().await()
        Result.success(snapshot.getValue(EventDto::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun joinGroup(groupId: Int): Result<Unit> = try {
        // Implementation for joining group in Firebase
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
