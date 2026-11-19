package com.example.warbage.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

data class BackendLot(
    val lotId: String,
    val collectorId: String,
    val materialId: Int,
    val weight: Double,
    val estimatedMinValue: Double,
    val estimatedMaxValue: Double,
    val description: String,
    val status: String,
    val dateCreated: String? = null
)

data class BackendRequest(
    val requestId: String,
    val lotId: String,
    val recyclerId: String,
    val status: String,
    val dateCreated: String? = null
)

data class BackendMaterial(
    val materialId: Int,
    val name: String,
    val description: String,
    val category: String,
    val recyclabilityScore: Int,
    val averagePricePerKg: Double
)

data class BackendPrice(
    val materialId: Int,
    val recyclerId: String,
    val pricePerKg: Double,
    val currency: String = "INR"
)

data class DeleteResponse(
    val success: Boolean,
    val message: String
)

interface WarbageApiService {

    @POST("/api/users")
    suspend fun createUser(@Body user: BackendUser): BackendUser

    @GET("/api/users")
    suspend fun getUser(@Query("userId") userId: String): List<BackendUser>

    @DELETE("/api/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): DeleteResponse

    @POST("/api/lots")
    suspend fun createLot(@Body lot: BackendLot): BackendLot

    @POST("/api/requests")
    suspend fun createRequest(@Body request: BackendRequest): BackendRequest

    @GET("/api/materials")
    suspend fun getMaterials(): List<BackendMaterial>
    
    @GET("/api/prices")
    suspend fun getPrices(): List<BackendPrice>
}
