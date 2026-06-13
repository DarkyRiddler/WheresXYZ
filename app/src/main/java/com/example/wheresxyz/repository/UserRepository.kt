package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.UserDto

interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<UserDto?>
    suspend fun updateProfile(userId: String, user: UserDto): Result<Unit>
}
