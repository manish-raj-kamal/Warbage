package com.example.warbage.data.repository

import com.example.warbage.data.dao.UserDao
import com.example.warbage.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getUser(uid: String): Flow<UserEntity?> = userDao.getUser(uid)

    suspend fun getUserSync(uid: String): UserEntity? = userDao.getUserSync(uid)

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    fun getLastActiveUser(): Flow<UserEntity?> = userDao.getLastActiveUser()

    suspend fun getLastActiveUserSync(): UserEntity? = userDao.getLastActiveUserSync()

    suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun clearUser() = userDao.clearUsers()
}

