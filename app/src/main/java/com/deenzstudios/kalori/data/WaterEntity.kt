package com.deenzstudios.kalori.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Jadual `water` — jumlah air diminum (ml) bagi setiap tarikh.
 * Satu baris = satu tarikh (format dd/MM/yyyy).
 */
@Entity(tableName = "water")
data class WaterEntity(
    @PrimaryKey val date: String,
    val consumedMl: Int = 0
)
