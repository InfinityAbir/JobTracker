package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.BuildConfig

/** Builds the pre-filled "Send Tester Feedback" email. */
object FeedbackHelper {

    fun deviceSummary(language: AppLanguage = LanguageManager.currentLanguage.value): String = buildString {
        appendLine("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        append("App language: ${language.name}")
    }

    fun subject(): String = "Job Tracker feedback (v${BuildConfig.VERSION_NAME})"

    fun body(language: AppLanguage = LanguageManager.currentLanguage.value): String = buildString {
        appendLine("What happened / what would you like to see?")
        appendLine()
        appendLine()
        appendLine("Steps to reproduce (for a bug):")
        appendLine("1. ")
        appendLine()
        appendLine("What did you expect instead?")
        appendLine()
        appendLine()
        appendLine("--- Device details (please keep) ---")
        append(deviceSummary(language))
    }

    fun buildIntent(recipient: String = BuildConfig.FEEDBACK_EMAIL): Intent {
        // mailto: restricts the chooser to email apps; subject and body travel as extras.
        val uri = if (recipient.isBlank()) Uri.parse("mailto:") else Uri.parse("mailto:${Uri.encode(recipient)}")
        return Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject())
            putExtra(Intent.EXTRA_TEXT, body())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /** Returns false when no email app can handle the request. */
    fun send(context: Context): Boolean {
        return try {
            context.startActivity(buildIntent())
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
