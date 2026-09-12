package com.deenzstudios.kalori.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReportDao {

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE date = :date LIMIT 1")
    suspend fun findByDate(date: String): ReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(reports: List<ReportEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reports: List<ReportEntity>)

    @Query("DELETE FROM reports")
    suspend fun clearAll()

    @Query("UPDATE reports SET weight = :weight, bmr = :bmr, tdee = :tdee WHERE date = :date")
    suspend fun updateProfileFields(date: String, weight: String, bmr: String, tdee: String)

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int
}
