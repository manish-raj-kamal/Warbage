package com.example.warbage.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.warbage.data.entity.LotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LotDao {

    @Query("SELECT * FROM lots ORDER BY dateCreated DESC")
    fun getAllLots(): Flow<List<LotEntity>>

    @Query("SELECT * FROM lots WHERE id = :id")
    fun getLotById(id: Int): Flow<LotEntity?>

    @Query("SELECT * FROM lots WHERE id = :id")
    suspend fun getLotByIdOnce(id: Int): LotEntity?

    @Insert
    suspend fun insert(lot: LotEntity): Long

    @Query("UPDATE lots SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("SELECT COUNT(*) FROM lots")
    suspend fun getLotCount(): Int
}
