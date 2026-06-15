package com.example.wheresxyz.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {

    @Test
    fun displayLabel_joinsNameAndLastname() {
        val user = User(
            id = "uid_1",
            userCode = 1234,
            name = "Jan",
            lastname = "Kowalski",
            email = "jan@example.com"
        )

        assertEquals("Jan Kowalski", user.displayLabel())
    }

    @Test
    fun displayLabel_trimsWhitespace() {
        val user = User(
            id = "uid_1",
            userCode = 1234,
            name = "Jan",
            lastname = "",
            email = "jan@example.com"
        )

        assertEquals("Jan", user.displayLabel())
    }

    @Test
    fun locationKey_normalizesEmail() {
        val user = User(
            id = "uid_1",
            userCode = 1234,
            name = "Jan",
            lastname = "Kowalski",
            email = "  Jan@Example.COM  "
        )

        assertEquals("jan@example.com", user.locationKey())
    }
}
