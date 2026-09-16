package com.example.warbage.data.repository

import com.example.warbage.data.dao.RecyclerDao
import com.example.warbage.data.entity.RecyclerEntity
import kotlinx.coroutines.flow.Flow

class RecyclerRepository(private val recyclerDao: RecyclerDao) {

    fun getAllRecyclers(): Flow<List<RecyclerEntity>> = recyclerDao.getAllRecyclers()

    fun getRecyclerById(id: Int): Flow<RecyclerEntity?> = recyclerDao.getRecyclerById(id)

    suspend fun getRecyclerByIdOnce(id: Int): RecyclerEntity? = recyclerDao.getRecyclerByIdOnce(id)
}
