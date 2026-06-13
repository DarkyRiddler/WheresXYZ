package com.example.wheresxyz.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInputValidatorTest {

    @Test
    fun validateLoginInput_rejectsBlankFields() {
        val result = validateLoginInput("", "password123")
        assertTrue(result.isFailure)
        assertEquals("Email and password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun validateLoginInput_rejectsInvalidEmail() {
        val result = validateLoginInput("not-an-email", "password123")
        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun validateLoginInput_rejectsShortPassword() {
        val result = validateLoginInput("user@example.com", "12345")
        assertTrue(result.isFailure)
        assertEquals("Password must be at least 6 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun validateLoginInput_acceptsValidInput() {
        val result = validateLoginInput("user@example.com", "password123")
        assertTrue(result.isSuccess)
    }

    @Test
    fun validateRegisterInput_rejectsMissingFields() {
        val result = validateRegisterInput("Jan", "", "user@example.com", "password123")
        assertTrue(result.isFailure)
        assertEquals("All fields are required", result.exceptionOrNull()?.message)
    }

    @Test
    fun validateRegisterInput_acceptsValidInput() {
        val result = validateRegisterInput("Jan", "Kowalski", "user@example.com", "password123")
        assertTrue(result.isSuccess)
    }

    @Test
    fun validateOAuthToken_rejectsBlankToken() {
        val result = validateOAuthToken(" ")
        assertTrue(result.isFailure)
        assertEquals("OAuth token is invalid", result.exceptionOrNull()?.message)
    }
}
