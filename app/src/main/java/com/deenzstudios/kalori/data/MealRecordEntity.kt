package com.deenzstudios.kalori.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Jadual `meal_records` — rekod makanan harian (menggantikan MySQL).
 * Setiap baris = satu item makanan dalam satu sesi makan pada satu tarikh.
 */
@Entity(
    tableName = "meal_records",
    indices = [Index(value = ["date", "mealType"])]
)
data class MealRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Tarikh format dd/MM/yyyy (sama seperti paparan). */
    val date: String,
    /** "Sarapan" | "Tengah Hari" | "Makan Malam". */
    val mealType: String,
    /** Teks paparan item (cth: "• Nasi (250g) = 345 kcal"). */
    val foodName: String,
    val calories: Double,
    val createdAt: Long = System.currentTimeMillis()
)
