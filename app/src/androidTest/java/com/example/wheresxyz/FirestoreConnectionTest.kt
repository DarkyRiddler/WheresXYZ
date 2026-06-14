package com.example.wheresxyz

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wheresxyz.data.remote.model.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FirestoreConnectionTest {

    @Test
    fun testFirestoreConnection_AddAndGetUser() = runBlocking {
        val db = FirebaseFirestore.getInstance()
        val testId = "test_user_" + UUID.randomUUID().toString().take(8)
        
        val testUser = UserDto(
            id = testId,
            userCode = 1234,
            name = "Test",
            lastname = "Connection",
            email = "test@example.com",
            userPhoto = "http://example.com/photo.jpg"
        )

        // 1. Write to Firestore
        db.collection("users").document(testId).set(testUser).await()

        // 2. Read from Firestore
        val document = db.collection("users").document(testId).get().await()
        val fetchedUser = document.toObject(UserDto::class.java)

        // 3. Verify
        assertNotNull("Fetched user should not be null", fetchedUser)
        assertEquals("IDs should match", testUser.id, fetchedUser?.id)
        assertEquals("Names should match", testUser.name, fetchedUser?.name)
        assertEquals("UserCode should match", testUser.userCode, fetchedUser?.userCode)
        
        // Cleanup (optional but good practice)
        db.collection("users").document(testId).delete().await()
    }
}
