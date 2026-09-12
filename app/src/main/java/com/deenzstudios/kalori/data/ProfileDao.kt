package com.deenzstudios.kalori.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = ProfileEntity.PROFILE_ID): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity)
}
