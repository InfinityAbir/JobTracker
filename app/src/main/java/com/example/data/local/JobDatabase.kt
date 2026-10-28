package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [JobApplicationEntity::class],
    version = 4,
    exportSchema = true
)
abstract class JobDatabase : RoomDatabase() {

    abstract fun jobApplicationDao(): JobApplicationDao

    companion object {
        const val DATABASE_NAME = "job_tracker_db"

        /** v3 -> v4: offer comparison, interview prep notes, analytics timestamps. Additive only. */
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "workModel", "offerTotalComp", "offerBenefits", "offerPros", "offerCons",
                    "prepQuestions", "prepStar", "prepNegotiation"
                ).forEach { column ->
                    db.execSQL("ALTER TABLE job_applications ADD COLUMN $column TEXT NOT NULL DEFAULT ''")
                }
                db.execSQL("ALTER TABLE job_applications ADD COLUMN interviewingAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE job_applications ADD COLUMN lastFollowUpAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Every schema change after the first Play release (v3) MUST add a Migration here so
        // updating the app keeps user data. Never add a destructive fallback for v3 or newer.
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_3_4)

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
