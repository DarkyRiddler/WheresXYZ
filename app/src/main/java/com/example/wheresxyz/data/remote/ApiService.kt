package com.example.wheresxyz.data.remote

import com.example.wheresxyz.data.remote.model.ApiAuthResponse
import com.example.wheresxyz.data.remote.model.ApiEventDto
import com.example.wheresxyz.data.remote.model.ApiGroupDto
import com.example.wheresxyz.data.remote.model.ApiUserDto
import com.example.wheresxyz.data.remote.model.CreateEventRequest
import com.example.wheresxyz.data.remote.model.CreateGroupRequest
import com.example.wheresxyz.data.remote.model.JoinGroupRequest
import com.example.wheresxyz.data.remote.model.LoginRequest
import com.example.wheresxyz.data.remote.model.OAuthRequest
import com.example.wheresxyz.data.remote.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): ApiAuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiAuthResponse

    @POST("api/auth/oauth")
    suspend fun loginWithOAuth(@Body body: OAuthRequest): ApiAuthResponse

    @GET("api/users/me")
    suspend fun getCurrentUser(@Header("Authorization") authorization: String): ApiUserDto

    @GET("api/groups")
    suspend fun getGroups(): List<ApiGroupDto>

    @POST("api/groups")
    suspend fun createGroup(@Body body: CreateGroupRequest): ApiGroupDto

    @POST("api/groups/{groupId}/join")
    suspend fun joinGroup(
        @Path("groupId") groupId: String,
        @Body body: JoinGroupRequest
    ): ApiGroupDto

    @GET("api/groups/{groupId}/event")
    suspend fun getGroupEvent(@Path("groupId") groupId: String): ApiEventDto

    @POST("api/events")
    suspend fun createEvent(@Body body: CreateEventRequest): ApiEventDto
}
