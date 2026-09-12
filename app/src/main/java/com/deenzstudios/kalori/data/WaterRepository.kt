package com.deenzstudios.kalori.data

import android.content.Context

/**
 * Repository penjejak air minuman berasaskan Room (SQLite) — OFFLINE.
 */
object WaterRepository {

    /** Sasaran harian (ml) jika pengguna belum set. */
    const val DEFAULT_TARGET_ML = 3000

    suspend fun getConsumed(context: Context, date: String): Int =
        AppDatabase.get(context).waterDao().getByDate(date)?.consumedMl ?: 0

    /** Tambah air diminum (ml) pada tarikh tertentu & pulangkan jumlah terkini. */
    suspend fun addWater(context: Context, date: String, ml: Int): Int {
        val dao = AppDatabase.get(context).waterDao()
        val updated = ((dao.getByDate(date)?.consumedMl ?: 0) + ml).coerceAtLeast(0)
        dao.upsert(WaterEntity(date = date, consumedMl = updated))
        return updated
    }

    /** Tetapkan semula jumlah air pada tarikh tertentu kepada 0. */
    suspend fun reset(context: Context, date: String) {
        AppDatabase.get(context).waterDao().upsert(WaterEntity(date = date, consumedMl = 0))
    }

    suspend fun getAll(context: Context): List<WaterEntity> =
        AppDatabase.get(context).waterDao().getAll()
}
