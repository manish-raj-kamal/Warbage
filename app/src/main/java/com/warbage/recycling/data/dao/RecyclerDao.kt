package com.warbage.recycling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.warbage.recycling.data.entity.RecyclerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecyclerDao {

    @Query("SELECT * FROM recyclers ORDER BY id")
    fun getAllRecyclers(): Flow<List<RecyclerEntity>>

    @Query("SELECT * FROM recyclers WHERE id = :id")
    fun getRecyclerById(id: Int): Flow<RecyclerEntity?>

    @Query("SELECT * FROM recyclers WHERE name = :name LIMIT 1")
    fun getRecyclerByName(name: String): Flow<RecyclerEntity?>

    @Query("SELECT * FROM recyclers WHERE id = :id")
    suspend fun getRecyclerByIdOnce(id: Int): RecyclerEntity?

    @Query("SELECT * FROM recyclers WHERE name = :name LIMIT 1")
    suspend fun getRecyclerByNameOnce(name: String): RecyclerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecycler(recycler: RecyclerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recyclers: List<RecyclerEntity>)
}
