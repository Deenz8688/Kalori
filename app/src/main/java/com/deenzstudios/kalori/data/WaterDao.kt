package com.deenzstudios.kalori.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WaterDao {

    @Query("SELECT * FROM water WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): WaterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WaterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<WaterEntity>)

    @Query("SELECT * FROM water ORDER BY date ASC")
    suspend fun getAll(): List<WaterEntity>

    @Query("DELETE FROM water")
    suspend fun clearAll()
}
