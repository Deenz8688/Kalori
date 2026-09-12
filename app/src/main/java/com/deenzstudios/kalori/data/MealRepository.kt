package com.deenzstudios.kalori.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository rekod makanan harian berasaskan Room (SQLite) — OFFLINE.
 * Menggantikan sync MySQL (specmb.org).
 */
object MealRepository {

    private const val MIGRATION_FLAG = "meal_room_migrated_v1"

    const val SARAPAN = "Sarapan"
    const val TENGAH_HARI = "Tengah Hari"
    const val MAKAN_MALAM = "Makan Malam"

    /** Tukar label beremoji (cth "🍳 Sarapan") kepada jenis standard. */
    fun normalizeMealType(label: String): String = when {
        label.contains("Sarapan", ignoreCase = true) -> SARAPAN
        label.contains("Tengah", ignoreCase = true) -> TENGAH_HARI
        else -> MAKAN_MALAM
    }

    suspend fun getByDate(context: Context, date: String): List<MealRecordEntity> =
        AppDatabase.get(context).mealRecordDao().getByDate(date)

    suspend fun add(
        context: Context,
        date: String,
        mealType: String,
        foodName: String,
        calories: Double
    ) {
        AppDatabase.get(context).mealRecordDao().insert(
            MealRecordEntity(
                date = date,
                mealType = mealType,
                foodName = foodName,
                calories = calories
            )
        )
    }

    suspend fun addAll(context: Context, records: List<MealRecordEntity>) {
        if (records.isNotEmpty()) AppDatabase.get(context).mealRecordDao().insertAll(records)
    }

    suspend fun deleteMeal(context: Context, date: String, mealType: String) {
        AppDatabase.get(context).mealRecordDao().deleteByDateAndMeal(date, mealType)
    }

    suspend fun getAllDates(context: Context): List<String> =
        AppDatabase.get(context).mealRecordDao().getAllDates()

    suspend fun totalFor(context: Context, date: String, mealType: String): Double =
        AppDatabase.get(context).mealRecordDao().getByDateAndMeal(date, mealType).sumOf { it.calories }

    /**
     * Pindahkan rekod lama dalam SharedPreferences ("UserProfile") ke Room.
     * Dijalankan sekali sahaja (dikawal oleh flag).
     */
    suspend fun migrateFromPrefs(context: Context, prefs: SharedPreferences) {
        if (prefs.getBoolean(MIGRATION_FLAG, false)) return

        val suffixes = mapOf(
            "_breakfast_text" to SARAPAN,
            "_lunch_text" to TENGAH_HARI,
            "_dinner_text" to MAKAN_MALAM
        )

        val rows = mutableListOf<MealRecordEntity>()
        for ((key, value) in prefs.all) {
            if (value !is String || value.isEmpty()) continue
            val suffix = suffixes.keys.firstOrNull { key.endsWith(it) } ?: continue
            val date = key.removeSuffix(suffix)
            if (!date.contains("/")) continue
            val totalKey = key.removeSuffix("_text") + "_total"
            val total = (prefs.all[totalKey] as? Float)?.toDouble() ?: 0.0
            rows.add(
                MealRecordEntity(
                    date = date,
                    mealType = suffixes.getValue(suffix),
                    foodName = value,
                    calories = total
                )
            )
        }

        if (rows.isNotEmpty()) AppDatabase.get(context).mealRecordDao().insertAll(rows)
        prefs.edit().putBoolean(MIGRATION_FLAG, true).apply()
    }
}
