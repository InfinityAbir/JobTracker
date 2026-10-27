package com.example.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.localization.AppStrings
import com.example.util.InterviewNotificationHelper

class InterviewReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        InterviewNotificationHelper.createNotificationChannel(context)

        val jobId = intent.getLongExtra(InterviewNotificationHelper.EXTRA_JOB_ID, 0L)
        val companyName = intent.getStringExtra(InterviewNotificationHelper.EXTRA_COMPANY_NAME) ?: "Company"
        val jobTitle = intent.getStringExtra(InterviewNotificationHelper.EXTRA_JOB_TITLE) ?: "Interview"
        val interviewType = intent.getStringExtra(InterviewNotificationHelper.EXTRA_INTERVIEW_TYPE) ?: ""
        val interviewNotes = intent.getStringExtra(InterviewNotificationHelper.EXTRA_INTERVIEW_NOTES) ?: ""
        val interviewTime = intent.getStringExtra(InterviewNotificationHelper.EXTRA_INTERVIEW_TIME) ?: ""

        // Main Tap Action: Opens Job Tracker App
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(InterviewNotificationHelper.EXTRA_JOB_ID, jobId)
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            jobId.toInt(),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val strings = AppStrings.current()
        val stageHeader = if (interviewType.isNotBlank()) "[${strings.getInterviewStageName(interviewType)}]" else strings.notifStageAlert
        val timeDisplay = if (interviewTime.isNotBlank()) strings.notifWhenAt(interviewTime) else strings.notifWhenSoonAlert

        val builder = NotificationCompat.Builder(context, InterviewNotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(strings.notifAlertTitle(companyName))
            .setContentText(strings.notifAlertContent(stageHeader, jobTitle, timeDisplay))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    buildString {
                        append(strings.notifAlertBig(stageHeader, companyName, jobTitle, timeDisplay)).append("\n")
                        if (interviewNotes.isNotBlank()) {
                            append("\n${strings.notifMeetingInfo}\n$interviewNotes")
                        }
                    }
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Check if interview notes contain a URL (e.g. meet.google.com or zoom.us or https://)
        val urlRegex = Regex("https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+")
        val match = urlRegex.find(interviewNotes)
        if (match != null) {
            val meetingUrl = match.value
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(meetingUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val meetingPendingIntent = PendingIntent.getActivity(
                context,
                jobId.toInt() + 20000,
                webIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                R.mipmap.ic_launcher,
                strings.notifJoin,
                meetingPendingIntent
            )
        }

        try {
            NotificationManagerCompat.from(context).notify(jobId.toInt(), builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
