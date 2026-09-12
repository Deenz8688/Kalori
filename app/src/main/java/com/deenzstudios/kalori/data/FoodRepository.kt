package com.deenzstudios.kalori.data

import android.content.Context

/**
 * Repository carian makanan berasaskan Room (SQLite) — OFFLINE.
 * CSV `foods_database.csv` di-seed ke dalam jadual `foods` sekali sahaja
 * pada first launch.
 */
object FoodRepository {

    private const val ASSET_NAME = "foods_database.csv"

    @Volatile
    private var seeding = false

    /** Seed CSV ke Room jika jadual masih kosong. Selamat dipanggil berulang kali. */
    suspend fun ensureSeeded(context: Context) {
        val dao = AppDatabase.get(context).foodDao()
        if (dao.count() > 0) return
        if (seeding) return
        seeding = true
        try {
            if (dao.count() == 0) {
                dao.insertAll(bacaCsv(context))
            }
        } finally {
            seeding = false
        }
    }

    suspend fun search(context: Context, query: String): List<FoodEntity> {
        ensureSeeded(context)
        return AppDatabase.get(context).foodDao().search(query)
    }

    suspend fun findByName(context: Context, name: String): FoodEntity? {
        ensureSeeded(context)
        return AppDatabase.get(context).foodDao().findByName(name)
    }

    /** Baca assets/foods_database.csv terus dari APK (tiada internet diperlukan). */
    private fun bacaCsv(context: Context): List<FoodEntity> {
        val senarai = mutableListOf<FoodEntity>()
        context.assets.open(ASSET_NAME).bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                if (index == 0 || line.isBlank()) return@forEachIndexed
                val kolum = pecahCsvLine(line)
                if (kolum.size < 5) return@forEachIndexed
                val nama = kolum[1].trim()
                if (nama.isEmpty()) return@forEachIndexed
                senarai.add(
                    FoodEntity(
                        name = nama,
                        serving = kolum[2].trim(),
                        gram = kolum[3].trim().toDoubleOrNull() ?: 0.0,
                        calories = kolum[4].trim().toDoubleOrNull() ?: 0.0,
                        unit = kolum.getOrElse(5) { "" }.trim()
                    )
                )
            }
        }
        return senarai
    }

    /** Pecah baris CSV dengan menghormati tanda petikan. */
    private fun pecahCsvLine(line: String): List<String> {
        val hasil = mutableListOf<String>()
        val current = StringBuilder()
        var dalamPetikan = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (dalamPetikan && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        dalamPetikan = !dalamPetikan
                    }
                }
                c == ',' && !dalamPetikan -> {
                    hasil.add(current.toString())
                    current.setLength(0)
                }
                else -> current.append(c)
            }
            i++
        }
        hasil.add(current.toString())
        return hasil
    }
}
