package com.warbage.recycling.data.repository

import android.util.Log
import com.warbage.recycling.data.dao.LotDao
import com.warbage.recycling.data.entity.LotEntity
import com.warbage.recycling.network.BackendLot
import com.warbage.recycling.network.RetrofitClient
import com.warbage.recycling.network.StatusUpdate
import kotlinx.coroutines.flow.Flow

class LotRepository(private val lotDao: LotDao) {

    fun getAllLots(): Flow<List<LotEntity>> = lotDao.getAllLots()

    fun getLotsByUser(userId: String): Flow<List<LotEntity>> = lotDao.getLotsByUser(userId)

    fun getLotById(id: Int): Flow<LotEntity?> = lotDao.getLotById(id)

    suspend fun getLotByIdOnce(id: Int): LotEntity? = lotDao.getLotByIdOnce(id)

    suspend fun insert(lot: LotEntity): Long {
        val id = lotDao.insert(lot)
        syncLotToBackend(lot)
        return id
    }

    suspend fun syncLotToBackend(lot: LotEntity) {
        try {
            val backendLot = BackendLot(
                lotId = lot.lotId,
                collectorId = lot.userId,
                collectorName = lot.collectorName,
                collectorEmail = lot.collectorEmail,
                collectorPhone = lot.collectorPhone,
                recyclerId = lot.recyclerId,
                recyclerName = lot.recyclerName,
                recyclerLocation = lot.recyclerLocation,
                recyclerPricePerKg = lot.recyclerPricePerKg,
                materialId = lot.materialId,
                weight = lot.weight,
                estimatedMinValue = lot.estimatedMinValue,
                estimatedMaxValue = lot.estimatedMaxValue,
                description = lot.description,
                status = lot.status.uppercase()
            )
            RetrofitClient.api.createLot(backendLot)
            Log.d("LotRepository", "Successfully synced lot ${lot.lotId} with collector & recycler data to backend")
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to sync lot ${lot.lotId} to backend", e)
        }
    }

    suspend fun updateRecycler(
        id: Int,
        recyclerId: String,
        recyclerName: String,
        recyclerLocation: String,
        pricePerKg: Double,
        status: String
    ) {
        lotDao.updateRecycler(id, recyclerId, recyclerName, recyclerLocation, pricePerKg, status)
        try {
            val updated = lotDao.getLotByIdOnce(id)
            if (updated != null) {
                syncLotToBackend(updated)
            }
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to sync updated recycler on lot", e)
        }
    }

    suspend fun updateStatus(id: Int, status: String) {
        lotDao.updateStatus(id, status)
        try {
            val lot = lotDao.getLotByIdOnce(id)
            if (lot != null) {
                RetrofitClient.api.updateLotStatus(lot.lotId, StatusUpdate(status.uppercase()))
                Log.d("LotRepository", "Synced lot status ${lot.lotId} -> $status to backend")
            }
        } catch (e: Exception) {
            Log.e("LotRepository", "Failed to sync lot status update to backend", e)
        }
    }

    suspend fun getNextLotNumber(): Int = lotDao.getLotCount() + 1
}
