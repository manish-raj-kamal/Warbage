package com.example.warbage.data.repository

import com.example.warbage.data.dao.LotDao
import com.example.warbage.data.entity.LotEntity
import com.example.warbage.network.BackendLot
import com.example.warbage.network.RetrofitClient
import kotlinx.coroutines.flow.Flow

class LotRepository(private val lotDao: LotDao) {

    fun getAllLots(): Flow<List<LotEntity>> = lotDao.getAllLots()

    fun getLotsByUser(userId: String): Flow<List<LotEntity>> = lotDao.getLotsByUser(userId)

    fun getLotById(id: Int): Flow<LotEntity?> = lotDao.getLotById(id)

    suspend fun getLotByIdOnce(id: Int): LotEntity? = lotDao.getLotByIdOnce(id)

    suspend fun insert(lot: LotEntity): Long {
        val id = lotDao.insert(lot)
        // Sync to backend
        try {
            val backendLot = BackendLot(
                lotId = lot.lotId,
                collectorId = lot.userId,
                materialId = lot.materialId,
                weight = lot.weight,
                estimatedMinValue = lot.estimatedMinValue,
                estimatedMaxValue = lot.estimatedMaxValue,
                description = lot.description,
                status = lot.status
            )
            RetrofitClient.api.createLot(backendLot)
        } catch (e: Exception) {
            android.util.Log.e("LotRepository", "Failed to sync lot to backend", e)
        }
        return id
    }

    suspend fun updateStatus(id: Int, status: String) = lotDao.updateStatus(id, status)

    suspend fun getNextLotNumber(): Int = lotDao.getLotCount() + 1
}
