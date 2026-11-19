package com.example.warbage.data.repository

import com.example.warbage.data.dao.RequestDao
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.network.BackendRequest
import com.example.warbage.network.RetrofitClient
import kotlinx.coroutines.flow.Flow

class RequestRepository(private val requestDao: RequestDao) {

    fun getAllRequests(): Flow<List<RequestEntity>> = requestDao.getAllRequests()

    fun getPendingRequests(): Flow<List<RequestEntity>> = requestDao.getPendingRequests()

    fun getRequestById(id: Int): Flow<RequestEntity?> = requestDao.getRequestById(id)

    suspend fun getRequestByIdOnce(id: Int): RequestEntity? = requestDao.getRequestByIdOnce(id)

    suspend fun insert(request: RequestEntity): Long {
        val id = requestDao.insert(request)
        // Sync to backend
        try {
            val backendReq = BackendRequest(
                requestId = request.requestId,
                lotId = request.lotId.toString(),
                recyclerId = request.recyclerId.toString(),
                status = request.status
            )
            RetrofitClient.api.createRequest(backendReq)
        } catch (e: Exception) {
            android.util.Log.e("RequestRepository", "Failed to sync request to backend", e)
        }
        return id
    }

    suspend fun updateStatus(id: Int, status: String) = requestDao.updateStatus(id, status)

    suspend fun getNextRequestNumber(): Int = requestDao.getRequestCount() + 1
}
