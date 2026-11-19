package com.warbage.recycling.data.repository

import com.warbage.recycling.data.dao.RecyclerDao
import com.warbage.recycling.data.entity.RecyclerEntity
import kotlinx.coroutines.flow.Flow

class RecyclerRepository(private val recyclerDao: RecyclerDao) {

    fun getAllRecyclers(): Flow<List<RecyclerEntity>> = recyclerDao.getAllRecyclers()

    fun getRecyclerById(id: Int): Flow<RecyclerEntity?> = recyclerDao.getRecyclerById(id)

    suspend fun getRecyclerByIdOnce(id: Int): RecyclerEntity? = recyclerDao.getRecyclerByIdOnce(id)
}
