package com.deenzstudios.kalori.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Jadual `profile` — profil pengguna (menggantikan SharedPreferences "UserProfile").
 * Hanya satu baris disimpan (id = 1).
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = PROFILE_ID,
    val name: String = "",
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val gender: String = "",
    val activity: String = "",
    val bmi: String = "",
    val bmiStatus: String = "",
    val bmiColor: Int = 0,
    val bmr: String = "",
    val tdee: String = "",
    /** Sasaran air minuman harian (ml). Default 3000 ml = 3 liter. */
    val waterTargetMl: Int = 3000,
    val profileImage: String? = null
) {
    companion object {
        const val PROFILE_ID = 1
    }
}
