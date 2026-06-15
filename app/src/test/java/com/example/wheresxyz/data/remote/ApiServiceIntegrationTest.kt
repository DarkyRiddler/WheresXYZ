package com.example.wheresxyz.data.remote

import com.example.wheresxyz.data.remote.model.LoginRequest
import com.example.wheresxyz.data.remote.model.RegisterRequest
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun login_deserializesAuthResponseFromBackendContract() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "accessToken": "jwt-token",
                      "refreshToken": "refresh-token",
                      "expiresIn": 3600,
                      "user": {
                        "id": "142",
                        "userCode": 8051,
                        "name": "John",
                        "lastname": "Doe",
                        "email": "johndoe@example.com",
                        "userPhoto": "https://api.wheresxyz.com/uploads/avatars/user_142.jpg"
                      }
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.login(
            LoginRequest(email = "johndoe@example.com", password = "secret123")
        )

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/auth/login", recordedRequest.path)
        assertNotNull(recordedRequest.body.readUtf8())

        assertEquals("jwt-token", response.accessToken)
        assertEquals("refresh-token", response.refreshToken)
        assertEquals(3600L, response.expiresIn)
        assertEquals("142", response.user.id)
        assertEquals(8051, response.user.userCode)
        assertEquals("johndoe@example.com", response.user.email)
    }

    @Test
    fun register_sendsExpectedPayloadAndParsesResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "accessToken": "jwt-token",
                      "refreshToken": null,
                      "expiresIn": 3600,
                      "user": {
                        "id": "99",
                        "userCode": 4321,
                        "name": "Anna",
                        "lastname": "Nowak",
                        "email": "anna@example.com"
                      }
                    }
                    """.trimIndent()
                )
        )

        val response = apiService.register(
            RegisterRequest(
                name = "Anna",
                lastname = "Nowak",
                email = "anna@example.com",
                password = "password123"
            )
        )

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/auth/register", recordedRequest.path)
        val requestBody = JsonParser.parseString(recordedRequest.body.readUtf8()).asJsonObject
        assertEquals("Anna", requestBody.get("name").asString)
        assertEquals("Nowak", requestBody.get("lastname").asString)
        assertEquals("anna@example.com", requestBody.get("email").asString)
        assertEquals("password123", requestBody.get("password").asString)

        assertEquals("Anna", response.user.name)
        assertEquals("Nowak", response.user.lastname)
        assertEquals(null, response.refreshToken)
    }

    @Test
    fun getGroups_deserializesGroupList() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    [
                      {
                        "id": "g1",
                        "name": "Znajomi",
                        "code": "1234",
                        "members": [
                          {
                            "name": "Jan",
                            "lastname": "Kowalski",
                            "email": "jan@example.com",
                            "canDelete": true,
                            "canModify": true,
                            "canCreateEvents": true
                          }
                        ]
                      }
                    ]
                    """.trimIndent()
                )
        )

        val groups = apiService.getGroups()

        assertEquals(1, groups.size)
        assertEquals("g1", groups.first().id)
        assertEquals("Znajomi", groups.first().name)
        assertEquals("jan@example.com", groups.first().members.first().email)
    }

    @Test
    fun login_returns401ThrowsHttpException() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":"Invalid credentials"}""")
        )

        val exception = assertThrows(HttpException::class.java) {
            runBlocking {
                apiService.login(LoginRequest(email = "bad@example.com", password = "wrong"))
            }
        }

        assertEquals(401, exception.code())
    }

    @Test
    fun getGroups_returns404ThrowsHttpException() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"error":"Not found"}""")
        )

        val exception = assertThrows(HttpException::class.java) {
            runBlocking { apiService.getGroups() }
        }

        assertEquals(404, exception.code())
    }

    @Test
    fun register_returns500ThrowsHttpException() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error":"Internal server error"}""")
        )

        val exception = assertThrows(HttpException::class.java) {
            runBlocking {
                apiService.register(
                    RegisterRequest(
                        name = "Anna",
                        lastname = "Nowak",
                        email = "anna@example.com",
                        password = "password123"
                    )
                )
            }
        }

        assertEquals(500, exception.code())
    }
}
