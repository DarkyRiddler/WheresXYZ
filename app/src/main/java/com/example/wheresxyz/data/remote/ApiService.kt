package com.example.wheresxyz.data.remote

import com.example.wheresxyz.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Authentication ---
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/oauth")
    suspend fun loginOAuth(@Body request: OAuthRequest): Response<AuthResponse>

    // --- Users ---
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserDto>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: UserDto): Response<UserDto>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>

    // --- Groups ---
    @GET("api/groups")
    suspend fun getGroups(): Response<List<GroupDto>>

    @POST("api/groups")
    suspend fun createGroup(@Body group: GroupDto): Response<GroupDto>

    @GET("api/groups/{id}")
    suspend fun getGroup(@Path("id") groupId: Int): Response<GroupDto>

    @PUT("api/groups/{id}")
    suspend fun updateGroup(@Path("id") groupId: Int, @Body group: GroupDto): Response<GroupDto>

    @DELETE("api/groups/{id}")
    suspend fun deleteGroup(@Path("id") groupId: Int): Response<Unit>

    // --- User-Group Relationships ---
    @POST("api/groups/{groupId}/join")
    suspend fun joinGroup(@Path("groupId") groupId: Int): Response<Unit>

    @DELETE("api/groups/{groupId}/leave")
    suspend fun leaveGroup(@Path("groupId") groupId: Int): Response<Unit>

    // --- Events (1:1 with Group) ---
    @GET("api/groups/{groupId}/event")
    suspend fun getGroupEvent(@Path("groupId") groupId: Int): Response<EventDto>

    @POST("api/events")
    suspend fun createEvent(@Body event: EventDto): Response<EventDto>

    @PUT("api/events/{id}")
    suspend fun updateEvent(@Path("id") eventId: Int, @Body event: EventDto): Response<EventDto>

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: Int): Response<Unit>

    // --- Pings (1:N with User) ---
    @GET("api/users/{userId}/pings")
    suspend fun getUserPings(@Path("userId") userId: Int): Response<List<PingDto>>

    @POST("api/pings")
    suspend fun sendPing(@Body ping: PingDto): Response<PingDto>

    @DELETE("api/pings/{id}")
    suspend fun deletePing(@Path("id") pingId: Int): Response<Unit>
}
