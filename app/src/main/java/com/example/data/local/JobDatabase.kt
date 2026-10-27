package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [JobApplicationEntity::class],
    version = 3,
    exportSchema = true
)
abstract class JobDatabase : RoomDatabase() {

    abstract fun jobApplicationDao(): JobApplicationDao

    companion object {
        const val DATABASE_NAME = "job_tracker_db"

        // Every schema change after the first Play release (v3) MUST add a Migration here so
        // updating the app keeps user data. Never add a destructive fallback for v3 or newer.
        val MIGRATIONS: Array<Migration> = arrayOf()

        @Volatile
        private var INSTANCE: JobDatabase? = null

        fun getInstance(context: Context): JobDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JobDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*MIGRATIONS)
                    // v1-v2 only ever existed in development builds, never on Play.
                    .fallbackToDestructiveMigrationFrom(true, 1, 2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
