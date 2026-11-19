package com.warbage.recycling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val minPrice: Int,
    val maxPrice: Int,
    val unit: String,
    val icon: String
)
