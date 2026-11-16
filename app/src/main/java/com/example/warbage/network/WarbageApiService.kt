package com.example.warbage.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// DTOs for the Backend
data class BackendUser(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val authProvider: String? = null
)

interface WarbageApiService {

    @POST("/api/users")
    suspend fun createUser(@Body user: BackendUser): BackendUser

    @GET("/api/users")
    suspend fun getUser(@Query("userId") userId: String): List<BackendUser>

}
