package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.JobTrackerApp
import com.example.data.mapper.toDomain
import com.example.util.InterviewNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val app = context.applicationContext as? JobTrackerApp ?: return
            val database = app.container.database
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val entities = database.jobApplicationDao().getAllDirect()
                    entities.forEach { entity ->
                        val domain = entity.toDomain()
                        if (domain.reminderSet && domain.interviewDate.isNotBlank()) {
                            InterviewNotificationHelper.scheduleInterviewAlarm(context, domain)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore restore errors on boot
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}