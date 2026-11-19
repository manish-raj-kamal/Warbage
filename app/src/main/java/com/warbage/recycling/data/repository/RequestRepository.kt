package com.warbage.recycling.data.repository

import android.util.Log
import com.warbage.recycling.data.dao.RequestDao
import com.warbage.recycling.data.entity.RequestEntity
import com.warbage.recycling.network.BackendRequest
import com.warbage.recycling.network.RetrofitClient
import com.warbage.recycling.network.StatusUpdate
import kotlinx.coroutines.flow.Flow

class RequestRepository(private val requestDao: RequestDao) {

    fun getAllRequests(): Flow<List<RequestEntity>> = requestDao.getAllRequests()

    fun getPendingRequests(): Flow<List<RequestEntity>> = requestDao.getPendingRequests()

    fun getRequestById(id: Int): Flow<RequestEntity?> = requestDao.getRequestById(id)

    suspend fun getRequestByIdOnce(id: Int): RequestEntity? = requestDao.getRequestByIdOnce(id)

    suspend fun insert(request: RequestEntity): Long {
        val id = requestDao.insert(request)
        syncRequestToBackend(request)
        return id
    }

    suspend fun syncRequestToBackend(request: RequestEntity) {
        try {
            val backendReq = BackendRequest(
                requestId = request.requestId,
                lotId = request.lotId.toString(),
                collectorId = request.userId,
                collectorName = request.collectorName,
                collectorPhone = request.collectorPhone,
                recyclerId = request.recyclerId.toString(),
                recyclerName = request.recyclerName,
                recyclerLocation = request.recyclerLocation,
                pricePerKg = request.pricePerKg,
                status = request.status.uppercase()
            )
            RetrofitClient.api.createRequest(backendReq)
            Log.d("RequestRepository", "Successfully synced request ${request.requestId} to backend")
        } catch (e: Exception) {
            Log.e("RequestRepository", "Failed to sync request ${request.requestId} to backend", e)
        }
    }

    suspend fun updateStatus(id: Int, status: String) {
        requestDao.updateStatus(id, status)
        try {
            val req = requestDao.getRequestByIdOnce(id)
            if (req != null) {
                RetrofitClient.api.updateRequestStatus(req.requestId, StatusUpdate(status.uppercase()))
                Log.d("RequestRepository", "Synced request status ${req.requestId} -> $status to backend")
            }
        } catch (e: Exception) {
            Log.e("RequestRepository", "Failed to sync request status update to backend", e)
        }
    }

    suspend fun getNextRequestNumber(): Int = requestDao.getRequestCount() + 1
}
