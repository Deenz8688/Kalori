package com.deenzstudios.kalori.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository profil pengguna berasaskan Room (SQLite) — OFFLINE.
 * Menggantikan SharedPreferences "UserProfile".
 */
object ProfileRepository {

    private const val MIGRATION_FLAG = "profile_room_migrated_v1"

    suspend fun getProfile(context: Context): ProfileEntity? =
        AppDatabase.get(context).profileDao().get()

    suspend fun saveProfile(context: Context, profile: ProfileEntity) {
        AppDatabase.get(context).profileDao().upsert(profile)
    }

    /**
     * Pindahkan profil lama dalam SharedPreferences ke Room.
     * Dijalankan sekali sahaja (dikawal oleh flag).
     */
    suspend fun migrateFromPrefs(context: Context, prefs: SharedPreferences) {
        if (prefs.getBoolean(MIGRATION_FLAG, false)) return

        if (prefs.getString("name", null) != null) {
            saveProfile(
                context,
                ProfileEntity(
                    name = prefs.getString("name", "") ?: "",
                    weight = prefs.getString("weight", "") ?: "",
                    height = prefs.getString("height", "") ?: "",
                    age = prefs.getString("age", "") ?: "",
                    gender = prefs.getString("gender", "") ?: "",
                    activity = prefs.getString("activity", "") ?: "",
                    bmi = prefs.getString("bmi", "") ?: "",
                    bmiStatus = prefs.getString("bmiStatus", "") ?: "",
                    bmiColor = prefs.getInt("bmiColor", 0),
                    bmr = prefs.getString("bmr", "") ?: "",
                    tdee = prefs.getString("tdee", "") ?: "",
                    profileImage = prefs.getString("profile_image", null)
                )
            )
        }

        prefs.edit().putBoolean(MIGRATION_FLAG, true).apply()
    }
}
