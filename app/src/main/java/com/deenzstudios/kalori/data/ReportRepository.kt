package com.deenzstudios.kalori.data

import android.content.Context

/**
 * Repository laporan harian berasaskan Room (SQLite) — OFFLINE.
 * Menggantikan SharedPreferences "ReportHistory".
 */
object ReportRepository {

    suspend fun getAll(context: Context): List<ReportEntity> =
        AppDatabase.get(context).reportDao().getAll()

    suspend fun findByDate(context: Context, date: String): ReportEntity? =
        AppDatabase.get(context).reportDao().findByDate(date)

    suspend fun save(context: Context, report: ReportEntity) {
        AppDatabase.get(context).reportDao().upsert(report)
    }

    suspend fun insertAll(context: Context, reports: List<ReportEntity>) {
        if (reports.isNotEmpty()) AppDatabase.get(context).reportDao().insertAll(reports)
    }

    suspend fun count(context: Context): Int =
        AppDatabase.get(context).reportDao().count()
}
