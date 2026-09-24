package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.JobApplication
import com.example.receiver.InterviewReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object InterviewNotificationHelper {

    const val CHANNEL_ID = "interview_reminders_channel"
    const val CHANNEL_NAME = "Interview & Timed Call Reminders"
    const val CHANNEL_DESC = "Exact alerts for scheduled job interviews, screening calls, and meeting rounds."

    const val EXTRA_JOB_ID = "extra_job_id"
    const val EXTRA_COMPANY_NAME = "extra_company_name"
    const val EXTRA_JOB_TITLE = "extra_job_title"
    const val EXTRA_INTERVIEW_TYPE = "extra_interview_type"
    const val EXTRA_INTERVIEW_NOTES = "extra_interview_notes"
    const val EXTRA_INTERVIEW_TIME = "extra_interview_time"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Calculates the trigger time in epoch milliseconds based on interview date and optional time.
     */
    fun calculateTriggerTimeMillis(
        dateStr: String,
        timeStr: String,
        minutesBefore: Int = 15
    ): Long? {
        if (dateStr.isBlank()) return null
        return try {
            val calendar = Calendar.getInstance()
            val cleanDate = dateStr.trim().take(10) // YYYY-MM-DD

            if (timeStr.isNotBlank()) {
                // Try format HH:mm or hh:mm a
                val formats = listOf("yyyy-MM-dd HH:mm", "yyyy-MM-dd hh:mm a", "yyyy-MM-dd h:mm a", "yyyy-MM-dd")
                var parsedDate: Date? = null
                for (fmt in formats) {
                    try {
                        val sdf = SimpleDateFormat(fmt, Locale.getDefault())
                        parsedDate = sdf.parse("$cleanDate ${timeStr.trim()}")
                        if (parsedDate != null) break
                    } catch (e: Exception) {
                        // try next
                    }
                }

                if (parsedDate != null) {
                    calendar.time = parsedDate
                } else {
                    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val d = sdfDate.parse(cleanDate) ?: return null
                    calendar.time = d
                    calendar.set(Calendar.HOUR_OF_DAY, 9)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                }
            } else {
                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val d = sdfDate.parse(cleanDate) ?: return null
                calendar.time = d
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }

            val targetMillis = calendar.timeInMillis - (minutesBefore * 60 * 1000L)
            targetMillis
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Schedules an exact alarm for the given job application.
     */
    fun scheduleInterviewAlarm(context: Context, job: JobApplication): Boolean {
        createNotificationChannel(context)

        val triggerTime = calculateTriggerTimeMillis(
            dateStr = job.interviewDate,
            timeStr = job.interviewTime,
            minutesBefore = job.reminderMinutesBefore
        ) ?: return false

        val now = System.currentTimeMillis()
        if (triggerTime <= now) {
            // Alarm time is in the past
            return false
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        val intent = Intent(context, InterviewReminderReceiver::class.java).apply {
            putExtra(EXTRA_JOB_ID, job.id)
            putExtra(EXTRA_COMPANY_NAME, job.companyName)
            putExtra(EXTRA_JOB_TITLE, job.jobTitle)
            putExtra(EXTRA_INTERVIEW_TYPE, job.interviewType)
            putExtra(EXTRA_INTERVIEW_NOTES, job.interviewNotes)
            putExtra(EXTRA_INTERVIEW_TIME, job.interviewTime.ifBlank { "Scheduled" })
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            job.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            true
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cancels an existing alarm for a job.
     */
    fun cancelInterviewAlarm(context: Context, jobId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, InterviewReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            jobId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Fires an immediate test notification so user can verify sounds, banners, and permission.
     */
    fun showTestNotification(context: Context, job: JobApplication) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_JOB_ID, job.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            job.id.toInt() + 10000,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stageText = if (job.interviewType.isNotBlank()) "[${job.interviewType}]" else "[Interview Round]"
        val timeDisplay = if (job.interviewTime.isNotBlank()) "at ${job.interviewTime}" else "soon"
        val noteSnippet = if (job.interviewNotes.isNotBlank()) "\nNotes/Link: ${job.interviewNotes}" else ""

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📅 Upcoming Interview: ${job.companyName}")
            .setContentText("$stageText ${job.jobTitle} is scheduled $timeDisplay$noteSnippet")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Reminder for ${job.jobTitle} at ${job.companyName}.\n$stageText $timeDisplay.$noteSnippet")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(job.id.toInt() + 5000, notification)
        } catch (e: SecurityException) {
            // Permission missing
        }
    }
}
