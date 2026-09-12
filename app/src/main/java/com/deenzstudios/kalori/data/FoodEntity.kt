package com.deenzstudios.kalori.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Jadual `foods` — salinan tempatan (SQLite) untuk foods_database.csv.
 * Data di-seed sekali sahaja pada first launch, jadi carian makanan
 * boleh dibuat tanpa internet.
 */
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val serving: String,
    val gram: Double,
    val calories: Double,
    val unit: String
)
