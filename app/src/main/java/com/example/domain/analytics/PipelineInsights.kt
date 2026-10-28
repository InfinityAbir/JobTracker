package com.example.domain.analytics

import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

/** Midnight (local time) of the given yyyy-MM-dd date, or null when the text is not a valid date. */
internal fun dayStartMillis(date: String): Long? {
    val text = date.trim().take(10)
    if (text.length != 10) return null
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        format.parse(text)?.time
    } catch (e: Exception) {
        null
    }
}

private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/** Whole calendar days from [fromMillis] to [toMillis]; negative if [toMillis] is earlier. */
internal fun daysBetween(fromMillis: Long, toMillis: Long): Int =
    ((startOfDay(toMillis) - startOfDay(fromMillis)).toDouble() / DAY_MILLIS).roundToInt()

/** True when the application made it to (or past) an interview, even if it was rejected afterwards. */
fun JobApplication.reachedInterview(): Boolean =
    status == JobStatus.INTERVIEWING ||
        status == JobStatus.OFFER ||
        interviewingAt > 0 ||
        interviewDate.isNotBlank()

data class SourceStat(
    /** Blank means no source was recorded. */
    val source: String,
    val applications: Int,
    val interviews: Int
) {
    val interviewRatePercent: Int
        get() = if (applications == 0) 0 else (interviews * 100.0 / applications).roundToInt()
}

data class PipelineAnalytics(
    val total: Int = 0,
    val last7Days: Int = 0,
    val last30Days: Int = 0,
    val interviewRatePercent: Int = 0,
    val responseRatePercent: Int = 0,
    val offerRatePercent: Int = 0,
    /** Average days from the applied date to the first Interviewing status; null when no data. */
    val avgDaysToInterview: Double? = null,
    val avgDaysSampleSize: Int = 0,
    val sources: List<SourceStat> = emptyList()
) {
    val isEmpty: Boolean get() = total == 0
}

object PipelineInsights {

    fun analyze(applications: List<JobApplication>, nowMillis: Long = System.currentTimeMillis()): PipelineAnalytics {
        if (applications.isEmpty()) return PipelineAnalytics()

        val total = applications.size
        var last7 = 0
        var last30 = 0
        for (job in applications) {
            val applied = dayStartMillis(job.dateApplied) ?: continue
            val age = daysBetween(applied, nowMillis)
            if (age in 0..6) last7++
            if (age in 0..29) last30++
        }

        val interviews = applications.count { it.reachedInterview() }
        val responses = applications.count { it.reachedInterview() || it.status == JobStatus.REJECTED }
        val offers = applications.count { it.status == JobStatus.OFFER }

        val waits = applications.mapNotNull { job ->
            if (job.interviewingAt <= 0) return@mapNotNull null
            val applied = dayStartMillis(job.dateApplied) ?: return@mapNotNull null
            max(0, daysBetween(applied, job.interviewingAt))
        }

        val sources = applications
            .groupBy { it.referralSource.trim() }
            .map { (source, jobs) -> SourceStat(source, jobs.size, jobs.count { it.reachedInterview() }) }
            .sortedWith(
                compareByDescending<SourceStat> { it.interviewRatePercent }
                    .thenByDescending { it.applications }
                    .thenBy { it.source }
            )

        return PipelineAnalytics(
            total = total,
            last7Days = last7,
            last30Days = last30,
            interviewRatePercent = percent(interviews, total),
            responseRatePercent = percent(responses, total),
            offerRatePercent = percent(offers, total),
            avgDaysToInterview = if (waits.isEmpty()) null else (waits.average() * 10).roundToInt() / 10.0,
            avgDaysSampleSize = waits.size,
            sources = sources
        )
    }

    private fun percent(part: Int, whole: Int): Int =
        if (whole == 0) 0 else (part * 100.0 / whole).roundToInt()
}

object StaleApplications {
    const val DEFAULT_THRESHOLD_DAYS = 14

    /**
     * Days since the last sign of life for an application still sitting in Applied, or null when it
     * is not stale. A drafted follow-up email counts as activity and restarts the clock.
     */
    fun daysWithoutUpdate(
        job: JobApplication,
        nowMillis: Long = System.currentTimeMillis(),
        thresholdDays: Int = DEFAULT_THRESHOLD_DAYS
    ): Int? {
        if (job.status != JobStatus.APPLIED) return null
        val appliedAt = dayStartMillis(job.dateApplied) ?: job.createdAt
        val lastActivity = max(appliedAt, job.lastFollowUpAt)
        val days = daysBetween(lastActivity, nowMillis)
        return if (days >= thresholdDays) days else null
    }
}
