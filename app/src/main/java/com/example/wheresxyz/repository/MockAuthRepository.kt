package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    
    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        delay(1000) // Symulacja opóźnienia sieci
        return if (request.email.contains("@")) {
            Result.success(createMockAuthResponse(request.email))
        } else {
            Result.failure(Exception("Invalid email format"))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        delay(1000)
        return Result.success(createMockAuthResponse(request.email))
    }

    override suspend fun loginOAuth(request: OAuthRequest): Result<AuthResponse> {
        delay(1000)
        return Result.success(createMockAuthResponse("oauth_user@example.com"))
    }

    override suspend fun logout() {
        // Mock logout
    }

    override suspend fun getAccessToken(): String? = "mock_token"

    private fun createMockAuthResponse(email: String) = AuthResponse(
        accessToken = "mock_access_token_jwt",
        refreshToken = "mock_refresh_token_uuid",
        expiresIn = 3600,
        user = UserDto(
            id = "1",
            userCode = 1234,
            name = "Test",
            lastname = "User",
            email = email,
            userPhoto = null
        )
    )
}
