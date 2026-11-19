package com.example.warbage.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.warbage.data.entity.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {

    @Query("SELECT * FROM requests ORDER BY dateCreated DESC")
    fun getAllRequests(): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE userId = :userId ORDER BY dateCreated DESC")
    fun getRequestsByUser(userId: String): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE recyclerId = :recyclerId AND (status = 'pending' OR status = 'accepted') ORDER BY dateCreated ASC")
    fun getPendingRequestsForRecycler(recyclerId: Int): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE recyclerId = :recyclerId ORDER BY dateCreated DESC")
    fun getAllRequestsForRecycler(recyclerId: Int): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE status = 'pending' OR status = 'accepted' ORDER BY dateCreated DESC")
    fun getPendingRequests(): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE id = :id")
    fun getRequestById(id: Int): Flow<RequestEntity?>

    @Query("SELECT * FROM requests WHERE id = :id")
    suspend fun getRequestByIdOnce(id: Int): RequestEntity?

    @Insert
    suspend fun insert(request: RequestEntity): Long

    @Query("UPDATE requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("SELECT COUNT(*) FROM requests")
    suspend fun getRequestCount(): Int
}
