package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.AuthResponse
import com.example.wheresxyz.data.remote.model.LoginRequest
import com.example.wheresxyz.data.remote.model.OAuthRequest
import com.example.wheresxyz.data.remote.model.RegisterRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun loginOAuth(request: OAuthRequest): Result<AuthResponse>
    suspend fun logout()
    suspend fun getAccessToken(): String?
}
