package com.example.warbage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.warbage.data.dao.LotDao
import com.example.warbage.data.dao.MaterialDao
import com.example.warbage.data.dao.RecyclerDao
import com.example.warbage.data.dao.RequestDao
import com.example.warbage.data.dao.UserDao
import com.example.warbage.data.entity.LotEntity
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.data.entity.RecyclerEntity
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MaterialEntity::class,
        RecyclerEntity::class,
        LotEntity::class,
        RequestEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class WarbageDatabase : RoomDatabase() {

    abstract fun materialDao(): MaterialDao
    abstract fun recyclerDao(): RecyclerDao
    abstract fun lotDao(): LotDao
    abstract fun requestDao(): RequestDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: WarbageDatabase? = null

        fun getDatabase(context: Context): WarbageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarbageDatabase::class.java,
                    "warbage_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateMaterials(database.materialDao())
                    populateRecyclers(database.recyclerDao())
                }
            }
        }

        private suspend fun populateMaterials(dao: MaterialDao) {
            dao.insertAll(
                listOf(
                    MaterialEntity(
                        id = 1,
                        name = "PCB",
                        description = "Printed Circuit Boards from computers, phones, and electronics",
                        minPrice = 450,
                        maxPrice = 600,
                        unit = "kg",
                        icon = "📟"
                    ),
                    MaterialEntity(
                        id = 2,
                        name = "Cables",
                        description = "Copper and aluminum wiring from electronics and appliances",
                        minPrice = 250,
                        maxPrice = 350,
                        unit = "kg",
                        icon = "🔌"
                    ),
                    MaterialEntity(
                        id = 3,
                        name = "Batteries",
                        description = "Li-ion, lead-acid, and NiMH batteries",
                        minPrice = 80,
                        maxPrice = 120,
                        unit = "kg",
                        icon = "🔋"
                    ),
                    MaterialEntity(
                        id = 4,
                        name = "LCD/LED",
                        description = "Display panels from monitors, TVs, and laptops",
                        minPrice = 60,
                        maxPrice = 100,
                        unit = "kg",
                        icon = "📺"
                    ),
                    MaterialEntity(
                        id = 5,
                        name = "Motors",
                        description = "Electric motors from appliances and machines",
                        minPrice = 150,
                        maxPrice = 250,
                        unit = "kg",
                        icon = "⚙️"
                    ),
                    MaterialEntity(
                        id = 6,
                        name = "CRT",
                        description = "Cathode Ray Tube glass from old TVs and monitors",
                        minPrice = 20,
                        maxPrice = 40,
                        unit = "kg",
                        icon = "🖥️"
                    ),
                    MaterialEntity(
                        id = 7,
                        name = "Mixed Plastics",
                        description = "Mixed plastic casings from electronic devices",
                        minPrice = 10,
                        maxPrice = 25,
                        unit = "kg",
                        icon = "♻️"
                    )
                )
            )
        }

        private suspend fun populateRecyclers(dao: RecyclerDao) {
            dao.insertAll(
                listOf(
                    RecyclerEntity(
                        id = 1,
                        name = "Green Recyclers",
                        location = "Mumbai",
                        latitude = 19.076,
                        longitude = 72.877,
                        materials = "PCB,Batteries,Cables",
                        pricePerKg = 550,
                        authorized = true
                    ),
                    RecyclerEntity(
                        id = 2,
                        name = "EcoTech Solutions",
                        location = "Pune",
                        latitude = 18.520,
                        longitude = 73.856,
                        materials = "LCD/LED,Motors,CRT",
                        pricePerKg = 180,
                        authorized = true
                    ),
                    RecyclerEntity(
                        id = 3,
                        name = "CleanE Waste",
                        location = "Delhi",
                        latitude = 28.613,
                        longitude = 77.209,
                        materials = "PCB,Cables,Mixed Plastics",
                        pricePerKg = 480,
                        authorized = true
                    ),
                    RecyclerEntity(
                        id = 4,
                        name = "RecycleHub India",
                        location = "Bangalore",
                        latitude = 12.971,
                        longitude = 77.594,
                        materials = "PCB,Cables,Batteries,LCD/LED,Motors,CRT,Mixed Plastics",
                        pricePerKg = 400,
                        authorized = true
                    )
                )
            )
        }
    }
}
