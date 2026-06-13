package com.example.wheresxyz.data.remote

import com.example.wheresxyz.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Authentication ---
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: UserDto): Response<AuthResponse>

    // --- Users ---
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserDto>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: UserDto): Response<UserDto>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>

    // --- Groups ---
    @GET("groups")
    suspend fun getGroups(): Response<List<GroupDto>>

    @POST("groups")
    suspend fun createGroup(@Body group: GroupDto): Response<GroupDto>

    @GET("groups/{id}")
    suspend fun getGroup(@Path("id") groupId: Int): Response<GroupDto>

    @PUT("groups/{id}")
    suspend fun updateGroup(@Path("id") groupId: Int, @Body group: GroupDto): Response<GroupDto>

    @DELETE("groups/{id}")
    suspend fun deleteGroup(@Path("id") groupId: Int): Response<Unit>

    // --- User-Group Relationships ---
    @POST("groups/{groupId}/join")
    suspend fun joinGroup(@Path("groupId") groupId: Int): Response<Unit>

    @DELETE("groups/{groupId}/leave")
    suspend fun leaveGroup(@Path("groupId") groupId: Int): Response<Unit>

    // --- Events (1:1 with Group) ---
    @GET("groups/{groupId}/event")
    suspend fun getGroupEvent(@Path("groupId") groupId: Int): Response<EventDto>

    @POST("events")
    suspend fun createEvent(@Body event: EventDto): Response<EventDto>

    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") eventId: Int, @Body event: EventDto): Response<EventDto>

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: Int): Response<Unit>

    // --- Pings (1:N with User) ---
    @GET("users/{userId}/pings")
    suspend fun getUserPings(@Path("userId") userId: Int): Response<List<PingDto>>

    @POST("pings")
    suspend fun sendPing(@Body ping: PingDto): Response<PingDto>

    @DELETE("pings/{id}")
    suspend fun deletePing(@Path("id") pingId: Int): Response<Unit>
}
