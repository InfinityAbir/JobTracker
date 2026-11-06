package com.example.domain.analytics

import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus

/** When the next interview happens, relative to now. Rendered to text by the widget. */
sealed interface InterviewCountdown {
    /** Started less than the grace period ago. */
    data object Now : InterviewCountdown

    /** Starts later today at a known time, [minutes] from now. */
    data class InMinutes(val minutes: Long) : InterviewCountdown

    /** Today, but no readable time. */
    data object Today : InterviewCountdown

    data object Tomorrow : InterviewCountdown

    data class InDays(val days: Long) : InterviewCountdown
}

/** Everything the home-screen widget shows, computed from the saved applications. */
data class WidgetSnapshot(
    val applied: Int,
    val interviewing: Int,
    val offers: Int,
    val nextInterview: JobApplication?,
    val countdown: InterviewCountdown?,
    /** Applications sitting in Applied long enough to deserve a follow-up. */
    val followUpsDue: Int,
    /** Epoch millis when the widget content changes by itself (the next interview ends); null if never. */
    val nextChangeAtMillis: Long?
) {
    companion object {
        fun from(applications: List<JobApplication>, now: Long): WidgetSnapshot {
            val next = applications.upcomingInterviews(now).firstOrNull()
            return WidgetSnapshot(
                applied = applications.count { it.status == JobStatus.APPLIED },
                interviewing = applications.count { it.status == JobStatus.INTERVIEWING },
                offers = applications.count { it.status == JobStatus.OFFER },
                nextInterview = next,
                countdown = next?.let { countdownFor(it, now) },
                followUpsDue = applications.count { StaleApplications.daysWithoutUpdate(it, now) != null },
                nextChangeAtMillis = next?.interviewExpiryMillis()
            )
        }

        internal fun countdownFor(job: JobApplication, now: Long): InterviewCountdown? {
            val start = job.interviewStartMillis() ?: return null
            val hasTime = parseMinutesOfDay(job.interviewTime) != null
            val days = daysBetween(now, start)
            return when {
                hasTime && start <= now -> InterviewCountdown.Now
                hasTime && days == 0 -> InterviewCountdown.InMinutes(maxOf(1L, (start - now + 59_999) / 60_000))
                days <= 0 -> InterviewCountdown.Today
                days == 1 -> InterviewCountdown.Tomorrow
                else -> InterviewCountdown.InDays(days.toLong())
            }
        }
    }
}
