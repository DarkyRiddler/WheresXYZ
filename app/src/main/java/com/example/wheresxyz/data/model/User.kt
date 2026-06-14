package com.example.wheresxyz.data.model

data class User(
    val id: String = "",
    val userCode: Int = 0, // 4-digit code
    val name: String = "",
    val lastname: String = "",
    val email: String = "",
    val userPhoto: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long, // in seconds
    val user: User
)
