package com.example.warbage.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val email: String,
    val displayName: String,
    val role: String, // "collector" or "recycler"
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val authProvider: String = "email", // "email", "google", "guest"
    val createdAt: Long = System.currentTimeMillis(),
    val isPendingDeletion: Boolean = false,
    val deletionRequestedAt: Long = 0L
)

