package com.example.warbage.data.repository

import android.util.Log
import com.example.warbage.data.dao.MaterialDao
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.network.BackendMaterial
import com.example.warbage.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MaterialRepository(private val materialDao: MaterialDao) {

    fun getAllMaterials(): Flow<List<MaterialEntity>> = materialDao.getAllMaterials()

    fun getMaterialById(id: Int): Flow<MaterialEntity?> = materialDao.getMaterialById(id)

    suspend fun getMaterialByIdOnce(id: Int): MaterialEntity? = materialDao.getMaterialByIdOnce(id)

    suspend fun insertMaterial(material: MaterialEntity) = materialDao.insertMaterial(material)

    suspend fun updateMaterial(material: MaterialEntity) = materialDao.updateMaterial(material)

    suspend fun getNextId(): Int = (materialDao.getMaxId() ?: 0) + 1

    suspend fun syncWithBackend() {
        try {
            val remoteMaterials = RetrofitClient.api.getMaterials()
            if (remoteMaterials.isNotEmpty()) {
                val entities = remoteMaterials.map {
                    MaterialEntity(
                        id = it.materialId,
                        name = it.name,
                        description = it.description,
                        minPrice = (it.averagePricePerKg * 0.8).toInt(),
                        maxPrice = (it.averagePricePerKg * 1.2).toInt(),
                        unit = "kg",
                        icon = when (it.name.lowercase()) {
                            "battery" -> "🔋"
                            "keyboard" -> "⌨️"
                            "microwave" -> "♨️"
                            "mobile" -> "📱"
                            "mouse" -> "🖱️"
                            "pcb" -> "📟"
                            "player" -> "▶️"
                            "printer" -> "🖨️"
                            "television" -> "📺"
                            "washing machine" -> "🧺"
                            else -> "♻️"
                        }
                    )
                }
                materialDao.insertAll(entities)
                Log.d("MaterialRepository", "Successfully fetched and cached ${remoteMaterials.size} materials from backend")
            } else {
                // Backend is empty: push local catalog to backend
                val localMaterials = materialDao.getAllMaterials().first()
                for (local in localMaterials) {
                    try {
                        val backendMat = BackendMaterial(
                            materialId = local.id,
                            name = local.name,
                            description = local.description,
                            category = "E-Waste",
                            recyclabilityScore = 80,
                            averagePricePerKg = ((local.minPrice + local.maxPrice) / 2.0)
                        )
                        RetrofitClient.api.createMaterial(backendMat)
                    } catch (e: Exception) {
                        Log.w("MaterialRepository", "Failed to upload material ${local.name}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MaterialRepository", "Failed to sync materials with backend", e)
        }
    }
}
