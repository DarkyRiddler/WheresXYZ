package com.example.wheresxyz.repository

import com.example.wheresxyz.data.local.TokenManager
import com.example.wheresxyz.data.remote.ApiService
import com.example.wheresxyz.data.remote.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                tokenManager.saveToken(authData.accessToken, authData.refreshToken, authData.expiresIn.toLong())
                Result.success(authData)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                tokenManager.saveToken(authData.accessToken, authData.refreshToken, authData.expiresIn.toLong())
                Result.success(authData)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginOAuth(request: OAuthRequest): Result<AuthResponse> {
        return try {
            val response = apiService.loginOAuth(request)
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                tokenManager.saveToken(authData.accessToken, authData.refreshToken, authData.expiresIn.toLong())
                Result.success(authData)
            } else {
                Result.failure(Exception("OAuth login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenManager.clearToken()
    }

    override suspend fun getAccessToken(): String? {
        return tokenManager.getToken()
    }
}
