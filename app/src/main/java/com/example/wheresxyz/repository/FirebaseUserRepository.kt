package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    private val usersCollection = firestore.collection("users")

    override suspend fun getUserProfile(userId: String): Result<UserDto?> = try {
        val document = usersCollection.document(userId).get().await()
        Result.success(document.toObject(UserDto::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProfile(userId: String, user: UserDto): Result<Unit> = try {
        usersCollection.document(userId).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
