package com.example.wheresxyz.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FirebaseKeySanitizerTest {

    @Test
    fun sanitizeFirebaseKey_replacesForbiddenCharacters() {
        val input = "user.name#tag\$key[1]"

        assertEquals("user_name_tag_key_1_", sanitizeFirebaseKey(input))
    }

    @Test
    fun sanitizeFirebaseKey_leavesSafeEmailUnchanged() {
        val input = "jan_kowalski@example_com"

        assertEquals(input, sanitizeFirebaseKey(input))
    }
}
