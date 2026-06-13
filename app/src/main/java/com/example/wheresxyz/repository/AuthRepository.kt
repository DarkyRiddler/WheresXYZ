package com.example.wheresxyz.repository

import com.example.wheresxyz.data.local.TokenManager
import com.example.wheresxyz.data.remote.ApiService
import com.example.wheresxyz.data.remote.model.AuthRequest
import com.example.wheresxyz.data.remote.model.AuthResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(request: AuthRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                tokenManager.saveToken(authData.token)
                Result.success(authData)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.deleteToken()
    }

    suspend fun getAccessToken(): String? {
        return tokenManager.token.first()
    }
}
