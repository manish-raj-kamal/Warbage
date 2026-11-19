package com.example.warbage.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val requestId: String,
    val lotId: Int,
    val recyclerId: Int,
    val dateCreated: Long,
    val status: String
)
