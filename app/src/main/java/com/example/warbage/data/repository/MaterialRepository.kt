package com.example.warbage.data.repository

import com.example.warbage.data.dao.MaterialDao
import com.example.warbage.data.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

class MaterialRepository(private val materialDao: MaterialDao) {

    fun getAllMaterials(): Flow<List<MaterialEntity>> = materialDao.getAllMaterials()

    fun getMaterialById(id: Int): Flow<MaterialEntity?> = materialDao.getMaterialById(id)

    suspend fun getMaterialByIdOnce(id: Int): MaterialEntity? = materialDao.getMaterialByIdOnce(id)
}
