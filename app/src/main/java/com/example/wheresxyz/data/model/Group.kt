package com.example.wheresxyz.data.model

data class GroupMember(
    val name: String = "",
    val lastname: String = "",
    val avatar: String = "👤", // Emoji or URI
    val email: String = "",
    val canDelete: Boolean = false,
    val canModify: Boolean = false,
    val canCreateEvents: Boolean = false,
    val isMe: Boolean = false
)

data class GroupItem(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val members: List<GroupMember> = emptyList(),
    val isAdmin: Boolean = false
)
