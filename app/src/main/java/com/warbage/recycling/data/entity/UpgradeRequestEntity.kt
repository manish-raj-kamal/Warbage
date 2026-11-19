package com.warbage.recycling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upgrade_requests")
data class UpgradeRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val recyclerName: String,
    val location: String,
    val dateRequested: Long,
    val status: String = "pending"
)
