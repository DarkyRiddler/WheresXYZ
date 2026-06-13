package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.GroupDto
import com.example.wheresxyz.data.remote.model.EventDto

interface GroupRepository {
    suspend fun getAllGroups(): Result<List<GroupDto>>
    suspend fun createGroup(group: GroupDto): Result<Unit>
    suspend fun getGroupEvent(groupId: String): Result<EventDto?>
    suspend fun joinGroup(groupId: String): Result<Unit>
}
