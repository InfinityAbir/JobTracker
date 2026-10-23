package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.domain.repository.JobRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocalAutoBackupManager {

    private const val BACKUP_FILE_NAME = "job_tracker_autobackup.json"
    private const val PREFS_NAME = "job_tracker_backup_prefs"
    private const val KEY_LAST_BACKUP_TIME = "key_last_backup_time"
    private const val KEY_LAST_BACKUP_COUNT = "key_last_backup_count"

    /**
     * Automatically saves a persistent copy of all job applications locally on device.
     */
    fun saveAutoBackup(context: Context, applications: List<JobApplication>) {
        if (applications.isEmpty()) return

        try {
            val jsonString = serializeApplicationsToJson(applications)
            val candidateFiles = getBackupTargetFiles(context)

            for (file in candidateFiles) {
                try {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        fos.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                } catch (e: Exception) {
                    // Try next location
                }
            }

            // Save metadata
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_LAST_BACKUP_TIME, System.currentTimeMillis())
                .putInt(KEY_LAST_BACKUP_COUNT, applications.size)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Checks if the database is empty and automatically restores the latest local backup.
     * Returns the number of restored items, or 0 if nothing was restored.
     */
    suspend fun autoRestoreIfEmpty(context: Context, repository: JobRepository): Int {
        try {
            val currentApps = repository.getAllDirect()
            if (currentApps.isNotEmpty()) {
                // Database is already populated, keep as is
                return 0
            }

            val backedUpApps = loadAutoBackup(context) ?: return 0
            if (backedUpApps.isEmpty()) return 0

            repository.insertApplications(backedUpApps)

            // Reschedule active interview alarms
            for (app in backedUpApps) {
                if (app.reminderSet && app.interviewDate.isNotBlank()) {
                    InterviewNotificationHelper.scheduleInterviewAlarm(context, app)
                }
            }

            return backedUpApps.size
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * Scans candidate backup locations and reads the latest backup.
     */
    fun loadAutoBackup(context: Context): List<JobApplication>? {
        val candidateFiles = getBackupTargetFiles(context)

        for (file in candidateFiles) {
            if (file.exists() && file.length() > 0) {
                try {
                    val content = file.readText(Charsets.UTF_8)
                    val apps = parseJsonToApplications(content)
                    if (apps.isNotEmpty()) {
                        return apps
                    }
                } catch (e: Exception) {
                    // Try next file
                }
            }
        }
        return null
    }

    /**
     * Restores applications from a user-selected JSON file URI.
     */
    suspend fun restoreFromUri(context: Context, uri: Uri, repository: JobRepository): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val content = InputStreamReader(inputStream, Charsets.UTF_8).use { it.readText() }
            val apps = parseJsonToApplications(content)
            if (apps.isNotEmpty()) {
                repository.insertApplications(apps)
                // Also update local auto backup with these restored apps
                val allUpdated = repository.getAllDirect()
                saveAutoBackup(context, allUpdated)
                apps.size
            } else {
                0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun getLastSyncInfo(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastTime = prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
        val count = prefs.getInt(KEY_LAST_BACKUP_COUNT, 0)

        return if (lastTime > 0) {
            val dateStr = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(lastTime))
            "Auto-Saved: $count applications ($dateStr)"
        } else {
            "Auto-Save: Ready (saves automatically on every change)"
        }
    }

    private fun getBackupTargetFiles(context: Context): List<File> {
        val list = mutableListOf<File>()

        // 1. Documents directory in External Storage (Survives app reinstalls)
        try {
            val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val appDocs = File(docsDir, "JobTracker")
            list.add(File(appDocs, BACKUP_FILE_NAME))
        } catch (e: Exception) {
            // Ignore
        }

        // 2. App-specific External Files Dir
        try {
            val extDir = context.getExternalFilesDir(null)
            if (extDir != null) {
                list.add(File(extDir, BACKUP_FILE_NAME))
            }
        } catch (e: Exception) {
            // Ignore
        }

        // 3. App-specific Internal Storage
        try {
            list.add(File(context.filesDir, BACKUP_FILE_NAME))
        } catch (e: Exception) {
            // Ignore
        }

        return list
    }

    private fun serializeApplicationsToJson(applications: List<JobApplication>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("totalCount", applications.size)

        val array = JSONArray()
        for (job in applications) {
            val obj = JSONObject()
            obj.put("id", job.id)
            obj.put("companyName", job.companyName)
            obj.put("jobTitle", job.jobTitle)
            obj.put("dateApplied", job.dateApplied)
            obj.put("status", job.status.name)
            obj.put("notes", job.notes)
            obj.put("salary", job.salary)
            obj.put("recruiterName", job.recruiterName)
            obj.put("recruiterEmail", job.recruiterEmail)
            obj.put("recruiterPhone", job.recruiterPhone)
            obj.put("referralSource", job.referralSource)
            obj.put("interviewDate", job.interviewDate)
            obj.put("interviewTime", job.interviewTime)
            obj.put("interviewType", job.interviewType)
            obj.put("interviewNotes", job.interviewNotes)
            obj.put("reminderSet", job.reminderSet)
            obj.put("reminderMinutesBefore", job.reminderMinutesBefore)
            obj.put("createdAt", job.createdAt)
            array.put(obj)
        }
        root.put("applications", array)
        return root.toString(2)
    }

    fun parseJsonToApplications(jsonString: String): List<JobApplication> {
        val list = mutableListOf<JobApplication>()
        try {
            val root = JSONObject(jsonString)
            val array = root.optJSONArray("applications") ?: return list

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val statusStr = obj.optString("status", "APPLIED")
                val status = try {
                    JobStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    JobStatus.APPLIED
                }

                val app = JobApplication(
                    id = obj.optLong("id", 0L),
                    companyName = obj.optString("companyName", ""),
                    jobTitle = obj.optString("jobTitle", ""),
                    dateApplied = obj.optString("dateApplied", ""),
                    status = status,
                    notes = obj.optString("notes", ""),
                    salary = obj.optString("salary", ""),
                    recruiterName = obj.optString("recruiterName", ""),
                    recruiterEmail = obj.optString("recruiterEmail", ""),
                    recruiterPhone = obj.optString("recruiterPhone", ""),
                    referralSource = obj.optString("referralSource", ""),
                    interviewDate = obj.optString("interviewDate", ""),
                    interviewTime = obj.optString("interviewTime", ""),
                    interviewType = obj.optString("interviewType", ""),
                    interviewNotes = obj.optString("interviewNotes", ""),
                    reminderSet = obj.optBoolean("reminderSet", false),
                    reminderMinutesBefore = obj.optInt("reminderMinutesBefore", 15),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )

                if (app.companyName.isNotBlank() && app.jobTitle.isNotBlank()) {
                    list.add(app)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
