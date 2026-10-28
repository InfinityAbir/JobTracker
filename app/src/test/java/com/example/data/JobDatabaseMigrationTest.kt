package com.example.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JobDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Builds a database exactly as the Play release (schema v3) created it, then opens it with the
 * current Room database. Room validates the migrated schema against the v4 entity, so a mismatch
 * between MIGRATION_3_4 and the entity fails here instead of on testers' phones.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JobDatabaseMigrationTest {

    private val dbName = "migration_test_db"
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    private fun createV3Database() {
        val db = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(dbName), null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `job_applications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`companyName` TEXT NOT NULL, `jobTitle` TEXT NOT NULL, `dateApplied` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, `notes` TEXT NOT NULL, `salary` TEXT NOT NULL, " +
                "`recruiterName` TEXT NOT NULL, `recruiterEmail` TEXT NOT NULL, `recruiterPhone` TEXT NOT NULL, " +
                "`referralSource` TEXT NOT NULL, `interviewDate` TEXT NOT NULL, `interviewTime` TEXT NOT NULL, " +
                "`interviewType` TEXT NOT NULL, `interviewNotes` TEXT NOT NULL, `reminderSet` INTEGER NOT NULL, " +
                "`reminderMinutesBefore` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO job_applications (companyName, jobTitle, dateApplied, status, notes, salary, " +
                "recruiterName, recruiterEmail, recruiterPhone, referralSource, interviewDate, interviewTime, " +
                "interviewType, interviewNotes, reminderSet, reminderMinutesBefore, createdAt) VALUES " +
                "('Stripe', 'Staff Engineer', '2026-09-01', 'INTERVIEWING', 'my notes', '\$220k', " +
                "'Sam', 'sam@stripe.com', '', 'LinkedIn', '2026-09-20', '10:00 AM', " +
                "'Technical Round', 'meet.google.com/x', 1, 30, 1700000000000)"
        )
        db.version = 3
        db.close()
    }

    @Test
    fun migrate3to4_keepsExistingDataAndAddsDefaults() = runBlocking {
        createV3Database()

        val database = Room.databaseBuilder(context, JobDatabase::class.java, dbName)
            .addMigrations(*JobDatabase.MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        try {
            val rows = database.jobApplicationDao().getAllDirect()
            assertEquals(1, rows.size)
            val job = rows.first()

            // Original data untouched
            assertEquals("Stripe", job.companyName)
            assertEquals("INTERVIEWING", job.status)
            assertEquals("my notes", job.notes)
            assertEquals("LinkedIn", job.referralSource)
            assertTrue(job.reminderSet)
            assertEquals(30, job.reminderMinutesBefore)
            assertEquals(1700000000000, job.createdAt)

            // New v4 columns present with safe defaults
            assertEquals("", job.workModel)
            assertEquals("", job.offerTotalComp)
            assertEquals("", job.offerBenefits)
            assertEquals("", job.prepQuestions)
            assertEquals("", job.prepStar)
            assertEquals("", job.prepNegotiation)
            assertEquals(0L, job.interviewingAt)
            assertEquals(0L, job.lastFollowUpAt)
        } finally {
            database.close()
        }
    }

    @Test
    fun migratedDatabase_acceptsNewFieldsAfterwards() = runBlocking {
        createV3Database()

        val database = Room.databaseBuilder(context, JobDatabase::class.java, dbName)
            .addMigrations(*JobDatabase.MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        try {
            val dao = database.jobApplicationDao()
            val existing = dao.getAllDirect().first()
            dao.updateApplication(existing.copy(workModel = "Remote", offerPros = "Great team", lastFollowUpAt = 42L))
            val updated = dao.getApplicationByIdDirect(existing.id)!!
            assertEquals("Remote", updated.workModel)
            assertEquals("Great team", updated.offerPros)
            assertEquals(42L, updated.lastFollowUpAt)
        } finally {
            database.close()
        }
    }
}
