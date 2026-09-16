package com.example.warbage.data.repository

import com.example.warbage.data.dao.LotDao
import com.example.warbage.data.entity.LotEntity
import kotlinx.coroutines.flow.Flow

class LotRepository(private val lotDao: LotDao) {

    fun getAllLots(): Flow<List<LotEntity>> = lotDao.getAllLots()

    fun getLotById(id: Int): Flow<LotEntity?> = lotDao.getLotById(id)

    suspend fun getLotByIdOnce(id: Int): LotEntity? = lotDao.getLotByIdOnce(id)

    suspend fun insert(lot: LotEntity): Long = lotDao.insert(lot)

    suspend fun updateStatus(id: Int, status: String) = lotDao.updateStatus(id, status)

    suspend fun getNextLotNumber(): Int = lotDao.getLotCount() + 1
}
