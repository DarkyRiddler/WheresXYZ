package com.example.wheresxyz.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val groupId: String = "",
    val groupName: String = "",
    val createdBy: String = "",
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val allowedDistance: Double = 0.0 // in meters
) {
    val isActive: Boolean
        get() = System.currentTimeMillis() in startDate..endDate
}
