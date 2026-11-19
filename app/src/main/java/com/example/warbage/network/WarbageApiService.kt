package com.example.warbage.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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
    val collectorName: String? = null,
    val collectorEmail: String? = null,
    val collectorPhone: String? = null,
    val recyclerId: String? = null,
    val recyclerName: String? = null,
    val recyclerLocation: String? = null,
    val recyclerPricePerKg: Double? = null,
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
    val collectorId: String? = null,
    val collectorName: String? = null,
    val collectorPhone: String? = null,
    val recyclerId: String,
    val recyclerName: String? = null,
    val recyclerLocation: String? = null,
    val pricePerKg: Double? = null,
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

data class BackendTransaction(
    val transactionId: String? = null,
    val lotId: String? = null,
    val requestId: String? = null,
    val collectorId: String,
    val recyclerId: String,
    val materialId: Int,
    val weightKg: Double,
    val totalPrice: Double,
    val status: String = "COMPLETED",
    val notes: String? = null,
    val transactionDate: Long? = null
)

data class StatusUpdate(
    val status: String
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

    @GET("/api/lots")
    suspend fun getLots(@Query("status") status: String? = null): List<BackendLot>

    @POST("/api/lots")
    suspend fun createLot(@Body lot: BackendLot): BackendLot

    @PUT("/api/lots/{id}/status")
    suspend fun updateLotStatus(@Path("id") lotId: String, @Body status: StatusUpdate): BackendLot

    @GET("/api/requests")
    suspend fun getRequests(): List<BackendRequest>

    @POST("/api/requests")
    suspend fun createRequest(@Body request: BackendRequest): BackendRequest

    @PUT("/api/requests/{id}/status")
    suspend fun updateRequestStatus(@Path("id") requestId: String, @Body status: StatusUpdate): BackendRequest

    @GET("/api/materials")
    suspend fun getMaterials(): List<BackendMaterial>

    @POST("/api/materials")
    suspend fun createMaterial(@Body material: BackendMaterial): BackendMaterial

    @GET("/api/prices")
    suspend fun getPrices(): List<BackendPrice>

    @POST("/api/prices")
    suspend fun createPrice(@Body price: BackendPrice): BackendPrice

    @GET("/api/transactions")
    suspend fun getTransactions(): List<BackendTransaction>

    @POST("/api/transactions")
    suspend fun createTransaction(@Body transaction: BackendTransaction): BackendTransaction
}
