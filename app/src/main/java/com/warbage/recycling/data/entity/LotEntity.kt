package com.warbage.recycling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lots")
data class LotEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val lotId: String,
    val materialId: Int,
    val weight: Double,
    val estimatedMinValue: Double,
    val estimatedMaxValue: Double,
    val description: String,
    val dateCreated: Long,
    val status: String,
    // Collector details
    val collectorName: String = "",
    val collectorEmail: String = "",
    val collectorPhone: String = "",
    // Recycler details
    val recyclerId: String = "",
    val recyclerName: String = "",
    val recyclerLocation: String = "",
    val recyclerPricePerKg: Double = 0.0
)
