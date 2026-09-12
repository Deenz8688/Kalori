package com.deenzstudios.kalori.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Jadual `reports` — sejarah laporan harian (menggantikan SharedPreferences "ReportHistory").
 * Satu baris mewakili satu tarikh (format dd/MM/yyyy).
 */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val date: String,
    val weight: String,
    val breakfast: String,
    val lunch: String,
    val dinner: String,
    val total: String,
    val bmr: String,
    val tdee: String,
    val createdAt: Long = System.currentTimeMillis()
)
