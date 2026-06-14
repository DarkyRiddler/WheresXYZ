package com.example.wheresxyz.data.repository

import com.example.wheresxyz.data.local.TokenManager
import com.example.wheresxyz.data.model.AuthResponse
import com.example.wheresxyz.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
                    userCode = generateUniqueUserCode(),
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    lastname = "User",
                    email = email,
                    userPhoto = null
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
                userCode = generateUniqueUserCode(),
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

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Google sign-in failed: empty user"))

            // Try to fetch existing Firestore profile
            val document = withTimeout(8.seconds) {
                usersCollection.document(firebaseUser.uid).get().await()
            }
            var user = document.toObject(User::class.java)

            if (user == null) {
                // First-time Google login — create a profile
                val nameParts = (firebaseUser.displayName ?: "Google User").split(" ", limit = 2)
                user = User(
                    id = firebaseUser.uid,
                    userCode = generateUniqueUserCode(),
                    name = nameParts.getOrElse(0) { "Google" },
                    lastname = nameParts.getOrElse(1) { "User" },
                    email = firebaseUser.email ?: "",
                    userPhoto = null
                )
                withTimeout(8.seconds) {
                    usersCollection.document(firebaseUser.uid).set(user).await()
                }
            }

            val idTokenStr = firebaseUser.getIdToken(false).await().token ?: ""
            tokenManager.saveToken(
                token = idTokenStr,
                refreshToken = "firebase_refresh_${firebaseUser.uid}",
                expiresIn = 3600L
            )

            Result.success(AuthResponse(idTokenStr, "firebase_refresh_${firebaseUser.uid}", 3600L, user))
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
                        userCode = generateUniqueUserCode(),
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
                userCode = generateUniqueUserCode(),
                name = name,
                lastname = lastname,
                email = firebaseUser.email ?: "",
                userPhoto = userPhoto
            )
            val updated = current.copy(name = name, lastname = lastname, userPhoto = userPhoto)
            withTimeout(8.seconds) {
                usersCollection.document(firebaseUser.uid).set(updated).await()
            }

            // Propagate profile changes to all groups this user is a member of
            try {
                val groupsCollection = firestore.collection("Groups")
                val groupsSnapshot = withTimeout(8.seconds) {
                    groupsCollection.get().await()
                }
                val userEmail = updated.email.lowercase().trim()
                groupsSnapshot.documents.forEach { doc ->
                    val rawMembers = doc.get("members") as? List<Any?> ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val membersList = rawMembers.filter { it is Map<*, *> } as List<Map<String, Any>>
                    if (membersList.any { (it["email"] as? String ?: "").lowercase().trim() == userEmail }) {
                        val updatedMembersList = membersList.map { map ->
                            if ((map["email"] as? String ?: "").lowercase().trim() == userEmail) {
                                map.toMutableMap().apply {
                                    put("name", name)
                                    put("lastname", lastname)
                                    put("avatar", userPhoto ?: "👤")
                                }
                            } else {
                                map
                            }
                        }
                        withTimeout(8.seconds) {
                            doc.reference.update("members", updatedMembersList).await()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private suspend fun generateUniqueUserCode(): Int {
        while (true) {
            val candidate = Random.nextInt(1000, 10000)
            val existing = withTimeout(8.seconds) {
                usersCollection.whereEqualTo("user_code", candidate).get().await()
            }
            if (existing.isEmpty) return candidate
        }
    }
}
