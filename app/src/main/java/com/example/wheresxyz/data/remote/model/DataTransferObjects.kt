package com.example.wheresxyz.data.remote.model

import com.google.gson.annotations.SerializedName
import java.util.Date

// --- Auth Models ---

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

// --- Entity DTOs ---

data class UserDto(
    @SerializedName("user_id") val id: Int,
    @SerializedName("user_code") val code: Int,
    val name: String,
    val lastname: String,
    val email: String,
    @SerializedName("user_photo") val photo: String?
)

data class GroupDto(
    @SerializedName("group_id") val id: Int,
    @SerializedName("group_code") val code: Int,
    @SerializedName("group_name") val name: String,
    @SerializedName("accepted_distance") val acceptedDistance: Float,
    @SerializedName("group_photo") val photo: String?
)

data class EventDto(
    @SerializedName("event_id") val id: Int,
    @SerializedName("event_title") val title: String,
    @SerializedName("event_description") val description: String,
    @SerializedName("event_photo") val photo: String?,
    @SerializedName("event_date_start") val dateStart: Date,
    @SerializedName("event_date_end") val dateEnd: Date,
    @SerializedName("group_id") val groupId: Int
)

data class PingDto(
    @SerializedName("ping_id") val id: Int,
    val emoji: String,
    val text: String,
    @SerializedName("user_id") val userId: Int
)
