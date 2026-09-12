package com.deenzstudios.kalori

import android.content.Context
import com.deenzstudios.kalori.data.ReportEntity
import com.deenzstudios.kalori.data.ReportRepository
import org.json.JSONArray

/**
 * Pembalut nipis di atas [ReportRepository] (Room/SQLite) — OFFLINE.
 * Kekalkan API lama supaya pemanggil tak banyak berubah, tetapi fungsi kini `suspend`.
 */
object ReportManager {

    private const val PREF_NAME = "ReportHistory"
    private const val PREF_KEY = "report_list"
    private const val MIGRATION_FLAG = "report_room_migrated_v1"

    suspend fun saveReport(context: Context, reportData: ReportData) {
        ensureMigrated(context)
        ReportRepository.save(context, reportData.toEntity())
    }

    suspend fun getReports(context: Context): List<ReportData> {
        ensureMigrated(context)
        return ReportRepository.getAll(context).map { it.toReportData() }
    }

    /**
     * Pindahkan laporan lama (JSON dlm SharedPreferences) ke Room.
     * Dijalankan sekali sahaja (dikawal oleh flag).
     */
    private suspend fun ensureMigrated(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(MIGRATION_FLAG, false)) return

        val saved = prefs.getString(PREF_KEY, "[]") ?: "[]"
        val rows = mutableListOf<ReportEntity>()
        try {
            val jsonArray = JSONArray(saved)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                rows.add(
                    ReportEntity(
                        date = obj.optString("date"),
                        weight = obj.optString("weight"),
                        breakfast = obj.optString("breakfast"),
                        lunch = obj.optString("lunch"),
                        dinner = obj.optString("dinner"),
                        total = obj.optString("total"),
                        bmr = obj.optString("bmr"),
                        tdee = obj.optString("tdee")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ReportRepository.insertAll(context, rows.filter { it.date.isNotEmpty() })
        prefs.edit().putBoolean(MIGRATION_FLAG, true).apply()
    }

    private fun ReportData.toEntity() = ReportEntity(
        date = date,
        weight = weight,
        breakfast = breakfast,
        lunch = lunch,
        dinner = dinner,
        total = total,
        bmr = bmr,
        tdee = tdee
    )

    private fun ReportEntity.toReportData() = ReportData(
        date, weight, breakfast, lunch, dinner, total, bmr, tdee
    )
}
