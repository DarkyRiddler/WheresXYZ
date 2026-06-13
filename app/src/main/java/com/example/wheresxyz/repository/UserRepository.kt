package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.ApiService
import com.example.wheresxyz.data.remote.model.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUserProfile(userId: Int) = try {
        val response = apiService.getUser(userId)
        if (response.isSuccessful) Result.success(response.body())
        else Result.failure(Exception("Error fetching user: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProfile(userId: Int, user: UserDto) = try {
        val response = apiService.updateUser(userId, user)
        if (response.isSuccessful) Result.success(response.body())
        else Result.failure(Exception("Update failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
