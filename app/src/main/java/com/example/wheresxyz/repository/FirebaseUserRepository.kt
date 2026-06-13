package com.example.wheresxyz.repository

import com.example.wheresxyz.data.remote.model.UserDto
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserRepository @Inject constructor(
    private val database: FirebaseDatabase
) : UserRepository {
    private val usersRef = database.getReference("users")

    override suspend fun getUserProfile(userId: String): Result<UserDto?> = try {
        val snapshot = usersRef.child(userId).get().await()
        Result.success(snapshot.getValue(UserDto::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProfile(userId: String, user: UserDto): Result<Unit> = try {
        usersRef.child(userId).setValue(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
