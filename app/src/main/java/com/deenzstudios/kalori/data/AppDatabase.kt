package com.deenzstudios.kalori.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FoodEntity::class, MealRecordEntity::class, ProfileEntity::class, ReportEntity::class, WaterEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao

    abstract fun mealRecordDao(): MealRecordDao

    abstract fun profileDao(): ProfileDao

    abstract fun reportDao(): ReportDao

    abstract fun waterDao(): WaterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v2 -> v3 hanya MENAMBAH jadual `profile` dan `reports`.
         * Jadual lama (foods, meal_records) dikekalkan supaya data tak hilang.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `profile` (" +
                        "`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `weight` TEXT NOT NULL, " +
                        "`height` TEXT NOT NULL, `age` TEXT NOT NULL, `gender` TEXT NOT NULL, " +
                        "`activity` TEXT NOT NULL, `bmi` TEXT NOT NULL, `bmiStatus` TEXT NOT NULL, " +
                        "`bmiColor` INTEGER NOT NULL, `bmr` TEXT NOT NULL, `tdee` TEXT NOT NULL, " +
                        "`profileImage` TEXT, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reports` (" +
                        "`date` TEXT NOT NULL, `weight` TEXT NOT NULL, `breakfast` TEXT NOT NULL, " +
                        "`lunch` TEXT NOT NULL, `dinner` TEXT NOT NULL, `total` TEXT NOT NULL, " +
                        "`bmr` TEXT NOT NULL, `tdee` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`date`))"
                )
            }
        }

        /**
         * v3 -> v4 menambah jadual `water` dan lajur `waterTargetMl` pada `profile`.
         * Data sedia ada dikekalkan.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `water` (" +
                        "`date` TEXT NOT NULL, `consumedMl` INTEGER NOT NULL, PRIMARY KEY(`date`))"
                )
                db.execSQL(
                    "ALTER TABLE `profile` ADD COLUMN `waterTargetMl` INTEGER NOT NULL DEFAULT 3000"
                )
            }
        }

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kalori.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    // Jika pengguna masih di v1 (tiada laluan terus), bina semula jadual
                    // kosong — seed makanan & migrasi SharedPreferences akan jalan semula.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
