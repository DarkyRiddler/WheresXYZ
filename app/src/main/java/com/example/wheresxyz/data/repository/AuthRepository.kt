package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.model.AuthResponse
import com.example.wheresxyz.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(name: String, lastname: String, email: String, password: String): Result<AuthResponse>
    suspend fun loginWithOAuth(provider: String, token: String): Result<AuthResponse>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout()
    suspend fun updateProfile(name: String, lastname: String, userPhoto: String?): Result<User>
}
