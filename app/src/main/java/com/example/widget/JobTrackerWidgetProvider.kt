package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.JobTrackerApp
import com.example.MainActivity
import com.example.R
import com.example.domain.analytics.InterviewCountdown
import com.example.domain.analytics.WidgetSnapshot
import com.example.domain.model.JobApplication
import com.example.localization.AppStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Home-screen widget: the next upcoming interview with a live countdown, the Applied / Interviewing /
 * Offer counts, and how many applications are due a follow-up. Tapping it opens the app.
 */
class JobTrackerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        refresh(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // The clock, timezone or language changed, so every shown time may now be wrong.
            ACTION_REFRESH,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED -> requestUpdate(context)
            else -> super.onReceive(context, intent)
        }
    }

    override fun onDisabled(context: Context) {
        // Last widget removed: stop the refresh alarm.
        alarmManager(context)?.cancel(refreshIntent(context))
    }

    private fun refresh(context: Context, widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        val app = context.applicationContext as? JobTrackerApp ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val snapshot = WidgetSnapshot.from(app.container.jobRepository.getAllDirect(), now)
                val manager = AppWidgetManager.getInstance(context)
                widgetIds.forEach { id -> manager.updateAppWidget(id, buildViews(context, snapshot, now)) }
                scheduleNextRefresh(context, snapshot, now)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.widget.ACTION_REFRESH"

        private const val REFRESH_REQUEST_CODE = 4101
        private const val OPEN_APP_REQUEST_CODE = 4102
        private const val MAX_REFRESH_INTERVAL_MILLIS = 15L * 60 * 1000
        private const val MIN_REFRESH_INTERVAL_MILLIS = 60L * 1000

        /** Asks every placed widget to redraw. Cheap no-op when no widget is on the home screen. */
        fun requestUpdate(context: Context) {
            try {
                val manager = AppWidgetManager.getInstance(context)
                val component = ComponentName(context, JobTrackerWidgetProvider::class.java)
                val ids = manager.getAppWidgetIds(component)
                if (ids.isEmpty()) return
                val intent = Intent(context, JobTrackerWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                // The widget is a convenience; never let it break saving data.
                e.printStackTrace()
            }
        }

        private fun alarmManager(context: Context): AlarmManager? =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        private fun refreshIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                REFRESH_REQUEST_CODE,
                Intent(context, JobTrackerWidgetProvider::class.java).setAction(ACTION_REFRESH),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        /**
         * Keeps the countdown fresh and drops an interview from the widget as soon as it is over.
         * Uses an inexact alarm, so it needs no special permission.
         */
        private fun scheduleNextRefresh(context: Context, snapshot: WidgetSnapshot, now: Long) {
            val latest = now + MAX_REFRESH_INTERVAL_MILLIS
            val change = snapshot.nextChangeAtMillis?.plus(1_000L)
            val at = (if (change != null && change in now..latest) change else latest)
                .coerceAtLeast(now + MIN_REFRESH_INTERVAL_MILLIS)
            alarmManager(context)?.set(AlarmManager.RTC, at, refreshIntent(context))
        }

        internal fun buildViews(context: Context, snapshot: WidgetSnapshot, now: Long): RemoteViews {
            val strings = AppStrings.current()
            val views = RemoteViews(context.packageName, R.layout.widget_job_tracker)

            views.setTextViewText(R.id.widget_title, strings.appName.uppercase())

            views.setTextViewText(R.id.widget_applied_count, snapshot.applied.toString())
            views.setTextViewText(R.id.widget_applied_label, strings.statusApplied)
            views.setTextViewText(R.id.widget_interviewing_count, snapshot.interviewing.toString())
            views.setTextViewText(R.id.widget_interviewing_label, strings.statusInterviewing)
            views.setTextViewText(R.id.widget_offer_count, snapshot.offers.toString())
            views.setTextViewText(R.id.widget_offer_label, strings.statusOffer)

            if (snapshot.followUpsDue > 0) {
                views.setTextViewText(R.id.widget_followups, strings.widgetFollowUps(snapshot.followUpsDue))
                views.setViewVisibility(R.id.widget_followups, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_followups, View.GONE)
            }

            val next = snapshot.nextInterview
            if (next == null) {
                views.setViewVisibility(R.id.widget_interview_block, View.GONE)
                views.setViewVisibility(R.id.widget_empty_block, View.VISIBLE)
                views.setTextViewText(R.id.widget_empty_title, strings.widgetNoInterviews)
                views.setTextViewText(R.id.widget_empty_hint, strings.widgetNoInterviewsHint)
            } else {
                views.setViewVisibility(R.id.widget_interview_block, View.VISIBLE)
                views.setViewVisibility(R.id.widget_empty_block, View.GONE)
                views.setTextViewText(R.id.widget_label, strings.widgetNextInterview)
                views.setTextViewText(R.id.widget_countdown, countdownText(snapshot.countdown, strings))
                views.setTextViewText(R.id.widget_company, next.companyName)
                views.setTextViewText(R.id.widget_role, next.jobTitle)
                views.setTextViewText(R.id.widget_when, whenText(next, strings))
            }

            val openApp = PendingIntent.getActivity(
                context,
                OPEN_APP_REQUEST_CODE,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openApp)
            return views
        }

        internal fun countdownText(countdown: InterviewCountdown?, strings: AppStrings): String =
            when (countdown) {
                InterviewCountdown.Now -> strings.widgetHappeningNow
                is InterviewCountdown.InMinutes -> strings.widgetStartsIn(countdown.minutes)
                InterviewCountdown.Today -> strings.relativeToday
                InterviewCountdown.Tomorrow -> strings.relativeTomorrow
                is InterviewCountdown.InDays ->
                    if (countdown.days > 6) strings.relativeInWeeks(countdown.days / 7 + 1)
                    else strings.relativeInDays(countdown.days)
                null -> strings.relativeScheduled
            }

        internal fun whenText(job: JobApplication, strings: AppStrings): String = buildString {
            append(job.interviewDate)
            if (job.interviewTime.isNotBlank()) append(" · ").append(job.interviewTime)
            if (job.interviewType.isNotBlank()) append(" · ").append(strings.getInterviewStageName(job.interviewType))
        }
    }
}
