package com.warbage.recycling.data.dao

import androidx.room.*
import com.warbage.recycling.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUser(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserSync(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 1")
    fun getLastActiveUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastActiveUserSync(): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

