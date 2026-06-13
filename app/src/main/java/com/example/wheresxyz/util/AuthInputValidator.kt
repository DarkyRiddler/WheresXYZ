package com.example.wheresxyz.util

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun validateLoginInput(email: String, password: String): Result<Unit> {
    if (email.isBlank() || password.isBlank()) {
        return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
    }
    if (!EMAIL_REGEX.matches(email)) {
        return Result.failure(IllegalArgumentException("Invalid email format"))
    }
    if (password.length < 6) {
        return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
    }
    return Result.success(Unit)
}

fun validateRegisterInput(
    name: String,
    lastname: String,
    email: String,
    password: String
): Result<Unit> {
    if (name.isBlank() || lastname.isBlank() || email.isBlank() || password.isBlank()) {
        return Result.failure(IllegalArgumentException("All fields are required"))
    }
    if (!EMAIL_REGEX.matches(email)) {
        return Result.failure(IllegalArgumentException("Invalid email format"))
    }
    if (password.length < 6) {
        return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
    }
    return Result.success(Unit)
}

fun validateOAuthToken(token: String): Result<Unit> {
    if (token.isBlank()) {
        return Result.failure(IllegalArgumentException("OAuth token is invalid"))
    }
    return Result.success(Unit)
}
