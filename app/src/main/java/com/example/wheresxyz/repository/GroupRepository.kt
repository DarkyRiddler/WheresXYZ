package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.GroupDto
import com.example.wheresxyz.data.remote.model.EventDto

interface GroupRepository {
    suspend fun getAllGroups(): Result<List<GroupDto>>
    suspend fun createGroup(group: GroupDto): Result<Unit>
    suspend fun getGroupEvent(groupId: Int): Result<EventDto?>
    suspend fun joinGroup(groupId: Int): Result<Unit>
}
