package com.example.wheresxyz.data.remote.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val lastname: String,
    val email: String,
    val password: String
)

data class OAuthRequest(
    val provider: String,
    val idToken: String
)

data class ApiAuthResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long,
    val user: ApiUserDto
)

data class ApiUserDto(
    val id: String,
    val userCode: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val userPhoto: String? = null
)

data class ApiGroupDto(
    val id: String,
    val name: String,
    val code: String,
    val members: List<ApiGroupMemberDto> = emptyList()
)

data class ApiGroupMemberDto(
    val name: String,
    val lastname: String,
    val email: String,
    val avatar: String? = null,
    val canDelete: Boolean = false,
    val canModify: Boolean = false,
    val canCreateEvents: Boolean = false
)

data class CreateGroupRequest(
    val name: String
)

data class JoinGroupRequest(
    val code: String
)

data class ApiEventDto(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val groupId: String,
    val groupName: String,
    val createdBy: String,
    val startLatitude: Double,
    val startLongitude: Double,
    val allowedDistance: Double
)

data class CreateEventRequest(
    val title: String,
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val groupId: String,
    val startLatitude: Double,
    val startLongitude: Double,
    val allowedDistance: Double
)
