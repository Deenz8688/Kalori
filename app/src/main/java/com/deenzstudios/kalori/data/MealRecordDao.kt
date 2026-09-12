package com.deenzstudios.kalori.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealRecordDao {

    @Query("SELECT * FROM meal_records ORDER BY id ASC")
    suspend fun getAll(): List<MealRecordEntity>

    @Query("SELECT * FROM meal_records WHERE date = :date ORDER BY id ASC")
    suspend fun getByDate(date: String): List<MealRecordEntity>

    @Query("SELECT * FROM meal_records WHERE date = :date AND mealType = :mealType ORDER BY id ASC")
    suspend fun getByDateAndMeal(date: String, mealType: String): List<MealRecordEntity>

    @Insert
    suspend fun insert(record: MealRecordEntity): Long

    @Insert
    suspend fun insertAll(records: List<MealRecordEntity>)

    @Query("DELETE FROM meal_records WHERE date = :date AND mealType = :mealType")
    suspend fun deleteByDateAndMeal(date: String, mealType: String)

    @Query("SELECT DISTINCT date FROM meal_records")
    suspend fun getAllDates(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<MealRecordEntity>)

    @Query("DELETE FROM meal_records")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM meal_records")
    suspend fun count(): Int
}
