package com.deenzstudios.kalori.data

import android.content.Context
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

/**
 * Backup / pulihkan data Kalori dalam format JSON (Room/SQLite).
 *
 * Jadual `foods` TIDAK disertakan — ia di-seed semula dari asset
 * `foods_database.csv` setiap kali app dipasang, jadi tak perlu backup.
 */
object BackupManager {

    /** Versi format fail backup — naikkan bila struktur berubah. */
    const val VERSION = 2

    /** Hasilkan kandungan JSON penuh (profil + hidangan + laporan). */
    suspend fun exportJson(context: Context): String {
        val db = AppDatabase.get(context)
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        db.profileDao().get()?.let { p ->
            root.put(
                "profile",
                JSONObject().apply {
                    put("name", p.name)
                    put("weight", p.weight)
                    put("height", p.height)
                    put("age", p.age)
                    put("gender", p.gender)
                    put("activity", p.activity)
                    put("bmi", p.bmi)
                    put("bmiStatus", p.bmiStatus)
                    put("bmiColor", p.bmiColor)
                    put("bmr", p.bmr)
                    put("tdee", p.tdee)
                    put("waterTargetMl", p.waterTargetMl)
                    put("profileImage", p.profileImage ?: JSONObject.NULL)
                }
            )
        }

        val meals = JSONArray()
        db.mealRecordDao().getAll().forEach { m ->
            meals.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("date", m.date)
                    put("mealType", m.mealType)
                    put("foodName", m.foodName)
                    put("calories", m.calories)
                    put("createdAt", m.createdAt)
                }
            )
        }
        root.put("meals", meals)

        val reports = JSONArray()
        db.reportDao().getAll().forEach { r ->
            reports.put(
                JSONObject().apply {
                    put("date", r.date)
                    put("weight", r.weight)
                    put("breakfast", r.breakfast)
                    put("lunch", r.lunch)
                    put("dinner", r.dinner)
                    put("total", r.total)
                    put("bmr", r.bmr)
                    put("tdee", r.tdee)
                    put("createdAt", r.createdAt)
                }
            )
        }
        root.put("reports", reports)

        val water = JSONArray()
        db.waterDao().getAll().forEach { w ->
            water.put(
                JSONObject().apply {
                    put("date", w.date)
                    put("consumedMl", w.consumedMl)
                }
            )
        }
        root.put("water", water)

        return root.toString(2)
    }

    /**
     * Pulihkan data dari kandungan JSON. Data sedia ada akan DIGANTI (replace).
     * Semua tulisan berlaku dalam satu transaksi supaya selamat.
     */
    suspend fun importJson(context: Context, json: String) {
        val root = JSONObject(json)

        val profile = root.optJSONObject("profile")?.let { p ->
            ProfileEntity(
                name = p.optString("name"),
                weight = p.optString("weight"),
                height = p.optString("height"),
                age = p.optString("age"),
                gender = p.optString("gender"),
                activity = p.optString("activity"),
                bmi = p.optString("bmi"),
                bmiStatus = p.optString("bmiStatus"),
                bmiColor = p.optInt("bmiColor", 0),
                bmr = p.optString("bmr"),
                tdee = p.optString("tdee"),
                waterTargetMl = p.optInt("waterTargetMl", 3000),
                profileImage = if (p.isNull("profileImage")) null else p.optString("profileImage")
            )
        }

        val meals = mutableListOf<MealRecordEntity>()
        root.optJSONArray("meals")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                meals.add(
                    MealRecordEntity(
                        id = o.optLong("id", 0),
                        date = o.optString("date"),
                        mealType = o.optString("mealType"),
                        foodName = o.optString("foodName"),
                        calories = o.optDouble("calories", 0.0),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val reports = mutableListOf<ReportEntity>()
        root.optJSONArray("reports")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                reports.add(
                    ReportEntity(
                        date = o.optString("date"),
                        weight = o.optString("weight"),
                        breakfast = o.optString("breakfast"),
                        lunch = o.optString("lunch"),
                        dinner = o.optString("dinner"),
                        total = o.optString("total"),
                        bmr = o.optString("bmr"),
                        tdee = o.optString("tdee"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val water = mutableListOf<WaterEntity>()
        root.optJSONArray("water")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                water.add(
                    WaterEntity(
                        date = o.optString("date"),
                        consumedMl = o.optInt("consumedMl", 0)
                    )
                )
            }
        }

        val db = AppDatabase.get(context)
        db.withTransaction {
            if (profile != null) db.profileDao().upsert(profile)

            db.mealRecordDao().clearAll()
            if (meals.isNotEmpty()) db.mealRecordDao().upsertAll(meals)

            db.reportDao().clearAll()
            if (reports.isNotEmpty()) db.reportDao().upsertAll(reports)

            db.waterDao().clearAll()
            if (water.isNotEmpty()) db.waterDao().upsertAll(water)
        }
    }
}
