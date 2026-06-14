package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.local.TokenManager
import com.example.wheresxyz.data.model.AuthResponse
import com.example.wheresxyz.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val tokenManager: TokenManager
) {

    private val usersCollection = firestore.collection("Users")

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
            }

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed: empty user"))

            // Fetch user profile from Firestore with timeout
            val document = withTimeout(8.seconds) {
                usersCollection.document(firebaseUser.uid).get().await()
            }
            var user = document.toObject(User::class.java)

            if (user == null) {
                // Fallback: If profile doesn't exist in DB, create a default one
                user = User(
                    id = firebaseUser.uid,
                    userCode = Random.nextInt(1000, 9999),
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    lastname = "User",
                    email = email,
                    userPhoto = firebaseUser.photoUrl?.toString()
                )
                withTimeout(8.seconds) {
                    usersCollection.document(firebaseUser.uid).set(user).await()
                }
            }

            // Save tokens/session in local token manager
            val idToken = firebaseUser.getIdToken(false).await().token ?: ""
            tokenManager.saveToken(
                token = idToken,
                refreshToken = "firebase_refresh_${firebaseUser.uid}",
                expiresIn = 3600L
            )

            Result.success(AuthResponse(idToken, "firebase_refresh_${firebaseUser.uid}", 3600L, user))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        lastname: String,
        email: String,
        password: String
    ): Result<AuthResponse> {
        return try {
            if (name.isBlank() || lastname.isBlank() || email.isBlank() || password.isBlank()) {
                return Result.failure(IllegalArgumentException("All fields are required"))
            }

            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Registration failed: empty user"))

            val newUser = User(
                id = firebaseUser.uid,
                userCode = Random.nextInt(1000, 9999),
                name = name,
                lastname = lastname,
                email = email,
                userPhoto = null
            )

            // Save user profile in database with timeout
            withTimeout(8.seconds) {
                usersCollection.document(firebaseUser.uid).set(newUser).await()
            }

            // Save token
            val idToken = firebaseUser.getIdToken(false).await().token ?: ""
            tokenManager.saveToken(
                token = idToken,
                refreshToken = "firebase_refresh_${firebaseUser.uid}",
                expiresIn = 3600L
            )

            Result.success(AuthResponse(idToken, "firebase_refresh_${firebaseUser.uid}", 3600L, newUser))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithOAuth(provider: String, token: String): Result<AuthResponse> {
        return try {
            val userEmail = "oauth.${provider.lowercase()}@example.com"
            val mockUid = "oauth_user_${provider.lowercase()}"
            val existingDocument = withTimeout(8.seconds) {
                usersCollection.document(mockUid).get().await()
            }
            var user = existingDocument.toObject(User::class.java)

            if (user == null) {
                user = User(
                    id = mockUid,
                    userCode = Random.nextInt(1000, 9999),
                    name = "OAuth",
                    lastname = provider.replaceFirstChar { it.uppercase() },
                    email = userEmail,
                    userPhoto = null
                )
                withTimeout(8.seconds) {
                    usersCollection.document(mockUid).set(user).await()
                }
            }

            tokenManager.saveToken(token, "firebase_refresh_$mockUid", 3600L)
            Result.success(AuthResponse(token, "firebase_refresh_$mockUid", 3600L, user))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val document = withTimeout(8.seconds) {
                    usersCollection.document(firebaseUser.uid).get().await()
                }
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    val restoredUser = User(
                        id = firebaseUser.uid,
                        userCode = Random.nextInt(1000, 9999),
                        name = firebaseUser.displayName ?: "User",
                        lastname = "",
                        email = firebaseUser.email ?: "",
                        userPhoto = firebaseUser.photoUrl?.toString()
                    )
                    withTimeout(8.seconds) {
                        usersCollection.document(firebaseUser.uid).set(restoredUser).await()
                    }
                    Result.success(restoredUser)
                }
            } else {
                Result.failure(IllegalStateException("No firebase user is currently logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            // ignored
        }
        tokenManager.clearToken()
    }

    suspend fun updateProfile(name: String, lastname: String, userPhoto: String?): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not logged in"))
            val document = withTimeout(8.seconds) {
                usersCollection.document(firebaseUser.uid).get().await()
            }
            val current = document.toObject(User::class.java) ?: User(
                id = firebaseUser.uid,
                userCode = Random.nextInt(1000, 9999),
                name = name,
                lastname = lastname,
                email = firebaseUser.email ?: "",
                userPhoto = userPhoto
            )
            val updated = current.copy(name = name, lastname = lastname, userPhoto = userPhoto)
            withTimeout(8.seconds) {
                usersCollection.document(firebaseUser.uid).set(updated).await()
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
