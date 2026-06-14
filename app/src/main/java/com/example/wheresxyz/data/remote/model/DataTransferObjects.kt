package com.example.wheresxyz.data.remote.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.IgnoreExtraProperties

// --- Auth Models ---

data class RegisterRequest(
    val name: String = "",
    val lastname: String = "",
    val email: String = "",
    val password: String = ""
)

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)

data class OAuthRequest(
    val provider: String = "",
    val token: String = ""
)

data class AuthResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresIn: Int = 0,
    val user: UserDto = UserDto()
)

// --- Entity DTOs ---

@IgnoreExtraProperties
data class UserDto(
    @get:PropertyName("user_id") @set:PropertyName("user_id") var id: String = "",
    @get:PropertyName("user_code") @set:PropertyName("user_code") var userCode: Int = 0,
    var name: String = "",
    var lastname: String = "",
    var email: String = "",
    @get:PropertyName("user_photo") @set:PropertyName("user_photo") var userPhoto: String? = null,
    // Many-to-Many: List of group IDs
    var groups: List<String> = emptyList()
)

@IgnoreExtraProperties
data class GroupDto(
    @get:PropertyName("group_id") @set:PropertyName("group_id") var id: String = "",
    @get:PropertyName("group_code") @set:PropertyName("group_code") var code: Int = 0,
    @get:PropertyName("group_name") @set:PropertyName("group_name") var name: String = "",
    @get:PropertyName("accepted_distance") @set:PropertyName("accepted_distance") var acceptedDistance: Float = 0f,
    @get:PropertyName("group_photo") @set:PropertyName("group_photo") var photo: String? = null,
    // Many-to-Many: List of user UIDs
    var members: List<String> = emptyList(),
    // 1-to-1 with Event
    @get:PropertyName("event_id") @set:PropertyName("event_id") var eventId: String? = null
)

@IgnoreExtraProperties
data class EventDto(
    @get:PropertyName("event_id") @set:PropertyName("event_id") var id: String = "",
    @get:PropertyName("event_title") @set:PropertyName("event_title") var title: String = "",
    @get:PropertyName("event_description") @set:PropertyName("event_description") var description: String = "",
    @get:PropertyName("event_photo") @set:PropertyName("event_photo") var photo: String? = null,
    @get:PropertyName("event_date_start") @set:PropertyName("event_date_start") var dateStart: Long = 0L,
    @get:PropertyName("event_date_end") @set:PropertyName("event_date_end") var dateEnd: Long = 0L,
    @get:PropertyName("group_id") @set:PropertyName("group_id") var groupId: String = ""
)

@IgnoreExtraProperties
data class PingDto(
    @get:PropertyName("ping_id") @set:PropertyName("ping_id") var id: String = "",
    var emoji: String = "",
    var text: String = "",
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String = ""
)
