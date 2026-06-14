package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.GroupMember
import com.example.wheresxyz.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class GroupsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val groupsCollection = firestore.collection("Groups")

    suspend fun getGroupsForUser(userEmail: String): Result<List<GroupItem>> {
        return try {
            val snapshot = withTimeout(8.seconds) {
                groupsCollection.get().await()
            }
            val groups = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: ""
                val code = doc.getString("code") ?: ""
                val rawMembers = doc.get("members") as? List<Any?> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val membersList = rawMembers.filter { it is Map<*, *> } as List<Map<String, Any>>
                val members = membersList.map { map ->
                    GroupMember(
                        name = map["name"] as? String ?: "",
                        lastname = map["lastname"] as? String ?: "",
                        avatar = map["avatar"] as? String ?: "👤",
                        email = map["email"] as? String ?: "",
                        canDelete = map["canDelete"] as? Boolean ?: false,
                        canModify = map["canModify"] as? Boolean ?: false,
                        canCreateEvents = map["canCreateEvents"] as? Boolean ?: false,
                        isMe = (map["email"] as? String ?: "").lowercase().trim() == userEmail.lowercase().trim()
                    )
                }
                
                // Only include groups where the user is a member
                if (members.any { it.isMe }) {
                    val isAdmin = members.firstOrNull()?.isMe == true
                    GroupItem(
                        id = id,
                        name = name,
                        code = code,
                        members = members,
                        isAdmin = isAdmin
                    )
                } else {
                    null
                }
            }
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroup(groupName: String, currentUser: User): Result<GroupItem> {
        return try {
            val randomCode = generateUniqueGroupCode()
            val newMember = mapOf(
                "name" to currentUser.name,
                "lastname" to currentUser.lastname,
                "email" to currentUser.email,
                "avatar" to (currentUser.userPhoto ?: "👤"),
                "canDelete" to true,
                "canModify" to true,
                "canCreateEvents" to true
            )
            val docRef = groupsCollection.document()
            val groupData = mapOf(
                "name" to groupName,
                "code" to randomCode,
                "members" to listOf(newMember)
            )
            withTimeout(8.seconds) {
                docRef.set(groupData).await()
            }
            val groupItem = GroupItem(
                id = docRef.id,
                name = groupName,
                code = randomCode,
                members = listOf(
                    GroupMember(
                        name = currentUser.name,
                        lastname = currentUser.lastname,
                        avatar = currentUser.userPhoto ?: "👤",
                        email = currentUser.email,
                        canDelete = true,
                        canModify = true,
                        canCreateEvents = true,
                        isMe = true
                    )
                ),
                isAdmin = true
            )
            Result.success(groupItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroup(code: String, currentUser: User): Result<GroupItem> {
        return try {
            val snapshot = withTimeout(8.seconds) {
                groupsCollection.whereEqualTo("code", code).get().await()
            }
            val doc = snapshot.documents.firstOrNull() ?: return Result.failure(Exception("Nie znaleziono grupy o podanym kodzie."))
            
            val id = doc.id
            val name = doc.getString("name") ?: ""
            val rawMembers = doc.get("members") as? List<Any?> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val membersList = rawMembers.filter { it is Map<*, *> } as List<Map<String, Any>>
            
            // Check if already a member
            val alreadyMember = membersList.any { (it["email"] as? String ?: "").lowercase().trim() == currentUser.email.lowercase().trim() }
            if (alreadyMember) {
                return Result.failure(Exception("Jesteś już członkiem tej grupy."))
            }

            val newMember = mapOf(
                "name" to currentUser.name,
                "lastname" to currentUser.lastname,
                "email" to currentUser.email,
                "avatar" to (currentUser.userPhoto ?: "👤"),
                "canDelete" to false,
                "canModify" to false,
                "canCreateEvents" to false
            )
            
            val updatedMembers = membersList + newMember
            withTimeout(8.seconds) {
                doc.reference.update("members", updatedMembers).await()
            }

            val members = updatedMembers.map { map ->
                GroupMember(
                    name = map["name"] as? String ?: "",
                    lastname = map["lastname"] as? String ?: "",
                    avatar = map["avatar"] as? String ?: "👤",
                    email = map["email"] as? String ?: "",
                    canDelete = map["canDelete"] as? Boolean ?: false,
                    canModify = map["canModify"] as? Boolean ?: false,
                    canCreateEvents = map["canCreateEvents"] as? Boolean ?: false,
                    isMe = (map["email"] as? String ?: "").lowercase().trim() == currentUser.email.lowercase().trim()
                )
            }

            Result.success(
                GroupItem(
                    id = id,
                    name = name,
                    code = code,
                    members = members,
                    isAdmin = false
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGroupName(groupId: String, newName: String): Result<Unit> {
        return try {
            withTimeout(8.seconds) {
                groupsCollection.document(groupId).update("name", newName).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMemberPermissions(
        groupId: String,
        targetEmail: String,
        canDelete: Boolean,
        canModify: Boolean,
        canCreateEvents: Boolean
    ): Result<Unit> {
        return try {
            val doc = withTimeout(8.seconds) {
                groupsCollection.document(groupId).get().await()
            }
            val rawMembers = doc.get("members") as? List<Any?> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val membersList = rawMembers.filter { it is Map<*, *> } as List<Map<String, Any>>
            val updatedMembersList = membersList.map { map ->
                if ((map["email"] as? String ?: "").lowercase().trim() == targetEmail.lowercase().trim()) {
                    map.toMutableMap().apply {
                        put("canDelete", canDelete)
                        put("canModify", canModify)
                        put("canCreateEvents", canCreateEvents)
                    }
                } else {
                    map
                }
            }
            withTimeout(8.seconds) {
                groupsCollection.document(groupId).update("members", updatedMembersList).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(groupId: String, targetEmail: String): Result<Unit> {
        return try {
            val doc = withTimeout(8.seconds) {
                groupsCollection.document(groupId).get().await()
            }
            val rawMembers = doc.get("members") as? List<Any?> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val membersList = rawMembers.filter { it is Map<*, *> } as List<Map<String, Any>>
            val updatedMembersList = membersList.filter { 
                (it["email"] as? String ?: "").lowercase().trim() != targetEmail.lowercase().trim() 
            }
            withTimeout(8.seconds) {
                groupsCollection.document(groupId).update("members", updatedMembersList).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private suspend fun generateUniqueGroupCode(): String {
        while (true) {
            val candidate = (1000..9999).random().toString()
            val existing = withTimeout(8.seconds) {
                groupsCollection.whereEqualTo("code", candidate).get().await()
            }
            if (existing.isEmpty) return candidate
        }
    }
}
