package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    
    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed"))
            
            val userDto = UserDto(
                id = firebaseUser.uid,
                userCode = 0, // This would normally come from the database
                name = firebaseUser.displayName ?: "User",
                lastname = "",
                email = firebaseUser.email ?: "",
                userPhoto = firebaseUser.photoUrl?.toString()
            )

            android.util.Log.d("FirebaseAuth", "Login success for: ${firebaseUser.email}")

            Result.success(AuthResponse(
                accessToken = firebaseUser.uid,
                refreshToken = "",
                expiresIn = 3600,
                user = userDto
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Registration failed"))
            
            val userDto = UserDto(
                id = firebaseUser.uid,
                userCode = (1000..9999).random(),
                name = request.name,
                lastname = request.lastname,
                email = firebaseUser.email ?: "",
                userPhoto = null
            )
            
            // In a real app, you would save this userDto to Firebase Realtime Database here
            
            Result.success(AuthResponse(
                accessToken = firebaseUser.uid,
                refreshToken = "",
                expiresIn = 3600,
                user = userDto
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginOAuth(request: OAuthRequest): Result<AuthResponse> {
        return Result.failure(Exception("OAuth not implemented for Firebase yet"))
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun getAccessToken(): String? {
        return firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
    }
}
