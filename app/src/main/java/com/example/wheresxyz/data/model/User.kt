package com.example.wheresxyz.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    @PropertyName("user_id")
    var id: String = "",

    @get:PropertyName("user_code")
    @set:PropertyName("user_code")
    @PropertyName("user_code")
    var userCode: Int = 0,

    @get:PropertyName("name")
    @set:PropertyName("name")
    @PropertyName("name")
    var name: String = "",

    @get:PropertyName("lastname")
    @set:PropertyName("lastname")
    @PropertyName("lastname")
    var lastname: String = "",

    @get:PropertyName("email")
    @set:PropertyName("email")
    @PropertyName("email")
    var email: String = "",

    @get:PropertyName("user_photo")
    @set:PropertyName("user_photo")
    @PropertyName("user_photo")
    var userPhoto: String? = null,

    @get:PropertyName("groups")
    @set:PropertyName("groups")
    @PropertyName("groups")
    var groups: List<Any> = emptyList(),

    @get:PropertyName("pings")
    @set:PropertyName("pings")
    @PropertyName("pings")
    var pings: List<Any> = emptyList(),

    @get:PropertyName("fcm_token")
    @set:PropertyName("fcm_token")
    @PropertyName("fcm_token")
    var fcmToken: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long, // in seconds
    val user: User
)
