package com.example.wheresxyz.data.remote.model

import com.google.gson.annotations.SerializedName
import java.util.Date

// --- Auth Models ---

data class RegisterRequest(
    val name: String,
    val lastname: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class OAuthRequest(
    val provider: String,
    val token: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UserDto
)

// --- Entity DTOs ---

data class UserDto(
    val id: String = "",
    val userCode: Int = 0,
    val name: String = "",
    val lastname: String = "",
    val email: String = "",
    val userPhoto: String? = null
)

data class GroupDto(
    @SerializedName("group_id") val id: String = "",
    @SerializedName("group_code") val code: Int = 0,
    @SerializedName("group_name") val name: String = "",
    @SerializedName("accepted_distance") val acceptedDistance: Float = 0f,
    @SerializedName("group_photo") val photo: String? = null
)

data class EventDto(
    @SerializedName("event_id") val id: String = "",
    @SerializedName("event_title") val title: String = "",
    @SerializedName("event_description") val description: String = "",
    @SerializedName("event_photo") val photo: String? = null,
    @SerializedName("event_date_start") val dateStart: Long = 0L, // Using Long for Firebase timestamps
    @SerializedName("event_date_end") val dateEnd: Long = 0L,
    @SerializedName("group_id") val groupId: String = ""
)

data class PingDto(
    @SerializedName("ping_id") val id: String = "",
    val emoji: String = "",
    val text: String = "",
    @SerializedName("user_id") val userId: String = ""
)
