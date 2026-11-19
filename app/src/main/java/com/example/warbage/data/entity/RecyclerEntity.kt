package com.example.warbage.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recyclers")
data class RecyclerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val materials: String,
    val pricePerKg: Int,
    val authorized: Boolean
)
