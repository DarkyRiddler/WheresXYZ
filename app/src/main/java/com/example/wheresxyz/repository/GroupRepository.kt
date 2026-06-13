package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.ApiService
import com.example.wheresxyz.data.remote.model.GroupDto
import com.example.wheresxyz.data.remote.model.EventDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllGroups() = try {
        val response = apiService.getGroups()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createGroup(group: GroupDto) = apiService.createGroup(group)

    suspend fun getGroupEvent(groupId: Int) = apiService.getGroupEvent(groupId)
    
    suspend fun joinGroup(groupId: Int) = apiService.joinGroup(groupId)
}
