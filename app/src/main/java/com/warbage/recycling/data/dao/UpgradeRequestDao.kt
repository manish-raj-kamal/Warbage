package com.warbage.recycling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.warbage.recycling.data.entity.UpgradeRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UpgradeRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: UpgradeRequestEntity)

    @Query("SELECT * FROM upgrade_requests WHERE userId = :userId")
    fun getRequestsByUser(userId: String): Flow<List<UpgradeRequestEntity>>
}
