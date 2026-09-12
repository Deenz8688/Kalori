package com.deenzstudios.kalori.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FoodDao {

    /** Carian bebas (nama mengandungi kata kunci). */
    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 30")
    suspend fun search(query: String): List<FoodEntity>

    /** Padanan tepat (abaikan huruf besar/kecil) — untuk auto-isi hidangan. */
    @Query("SELECT * FROM foods WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): FoodEntity?

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<FoodEntity>)

    @Query("DELETE FROM foods")
    suspend fun clear()
}
