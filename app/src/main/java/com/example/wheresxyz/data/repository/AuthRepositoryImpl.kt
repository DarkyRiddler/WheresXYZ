package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.local.TokenManager
import com.example.wheresxyz.data.model.AuthResponse
import com.example.wheresxyz.data.model.User
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager
) : AuthRepository {

    // Simple in-memory user cache for demonstration/mock purposes
    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        delay(1500) // Simulate network delay
        
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        // Simulating success
        val mockUser = User(
            id = Random.nextInt(1, 1000),
            userCode = Random.nextInt(1000, 9999),
            name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            lastname = "User",
            email = email,
            userPhoto = null
        )

        val accessToken = UUID.randomUUID().toString()
        val refreshToken = UUID.randomUUID().toString()
        val expiresIn = 3600L // 1 hour

        tokenManager.saveToken(accessToken, refreshToken, expiresIn)
        currentUser = mockUser

        return Result.success(AuthResponse(accessToken, refreshToken, expiresIn, mockUser))
    }

    override suspend fun register(
        name: String,
        lastname: String,
        email: String,
        password: String
    ): Result<AuthResponse> {
        delay(1500) // Simulate network delay

        if (name.isBlank() || lastname.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        val mockUser = User(
            id = Random.nextInt(1, 1000),
            userCode = Random.nextInt(1000, 9999),
            name = name,
            lastname = lastname,
            email = email,
            userPhoto = null
        )

        val accessToken = UUID.randomUUID().toString()
        val refreshToken = UUID.randomUUID().toString()
        val expiresIn = 3600L

        tokenManager.saveToken(accessToken, refreshToken, expiresIn)
        currentUser = mockUser

        return Result.success(AuthResponse(accessToken, refreshToken, expiresIn, mockUser))
    }

    override suspend fun loginWithOAuth(provider: String, token: String): Result<AuthResponse> {
        delay(1000) // Simulate network delay

        if (token.isBlank()) {
            return Result.failure(IllegalArgumentException("OAuth token is invalid"))
        }

        // Mock OAuth validation and login
        val mockUser = User(
            id = Random.nextInt(1, 1000),
            userCode = Random.nextInt(1000, 9999),
            name = "OAuth",
            lastname = provider.replaceFirstChar { it.uppercase() },
            email = "oauth.${provider.lowercase()}@example.com",
            userPhoto = null
        )

        val accessToken = token
        val refreshToken = UUID.randomUUID().toString()
        val expiresIn = 3600L

        tokenManager.saveToken(accessToken, refreshToken, expiresIn)
        currentUser = mockUser

        return Result.success(AuthResponse(accessToken, refreshToken, expiresIn, mockUser))
    }

    override suspend fun getCurrentUser(): Result<User> {
        delay(500)
        currentUser?.let {
            return Result.success(it)
        }

        // If user is logged in via token but cache is empty, we restore a mock user
        if (tokenManager.isTokenValid()) {
            val restoredUser = User(
                id = 42,
                userCode = 7777,
                name = "Logged In",
                lastname = "User",
                email = "user@wheresxyz.com",
                userPhoto = null
            )
            currentUser = restoredUser
            return Result.success(restoredUser)
        }

        return Result.failure(IllegalStateException("No user is currently logged in"))
    }

    override suspend fun logout() {
        delay(300)
        tokenManager.clearToken()
        currentUser = null
    }

    override suspend fun updateProfile(name: String, lastname: String, userPhoto: String?): Result<User> {
        delay(1000)
        val current = currentUser ?: User(
            id = 42,
            userCode = 7777,
            name = "Witaj",
            lastname = "Użytkowniku",
            email = "user@wheresxyz.com",
            userPhoto = null
        )
        val updated = current.copy(name = name, lastname = lastname, userPhoto = userPhoto)
        currentUser = updated
        return Result.success(updated)
    }
}
