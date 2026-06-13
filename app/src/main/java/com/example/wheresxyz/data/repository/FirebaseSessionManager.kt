package com.example.wheresxyz.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun ensureAnonymousAuth(): Result<Unit> {
        return try {
            if (firebaseAuth.currentUser == null) {
                firebaseAuth.signInAnonymously().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
