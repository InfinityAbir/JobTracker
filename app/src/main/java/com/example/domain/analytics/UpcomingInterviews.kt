package com.example.domain.analytics

import com.example.domain.model.JobApplication
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** How long after its start time an interview still counts as upcoming (it may be running or just over). */
const val INTERVIEW_GRACE_MILLIS = 60L * 60 * 1000

private val TIME_FORMATS = listOf("hh:mm a", "h:mm a", "HH:mm", "H:mm")

/** Minutes after midnight for texts like "10:30 AM", "2:30 pm" or "14:30"; null if unreadable. */
internal fun parseMinutesOfDay(time: String): Int? {
    val text = time.trim()
    if (text.isEmpty()) return null
    for (pattern in TIME_FORMATS) {
        // A full match is required, otherwise "HH:mm" would accept the "10:30" of "10:30 PM".
        val format = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
        val position = ParsePosition(0)
        val parsed = format.parse(text, position) ?: continue
        if (position.index != text.length) continue
        val calendar = Calendar.getInstance().apply { this.time = parsed }
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }
    return null
}

/**
 * Epoch millis when the interview starts: the exact time when one is readable, otherwise the start
 * of that day. Null if the date is unreadable.
 */
fun JobApplication.interviewStartMillis(): Long? {
    val dayStart = dayStartMillis(interviewDate) ?: return null
    val minutes = parseMinutesOfDay(interviewTime) ?: return dayStart
    return Calendar.getInstance().apply {
        timeInMillis = dayStart
        add(Calendar.MINUTE, minutes)
    }.timeInMillis
}

/** Epoch millis when the interview is over for display purposes, or null if the date is unreadable. */
fun JobApplication.interviewExpiryMillis(): Long? {
    val start = interviewStartMillis() ?: return null
    return if (parseMinutesOfDay(interviewTime) == null) {
        // No usable time: keep the card for the whole day.
        Calendar.getInstance().apply {
            timeInMillis = start
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
    } else {
        start + INTERVIEW_GRACE_MILLIS
    }
}

/**
 * Interviews that are still ahead of [now], soonest first. Past interviews drop off automatically;
 * an interview with an unreadable date is kept so it is never silently lost.
 */
fun List<JobApplication>.upcomingInterviews(now: Long): List<JobApplication> =
    filter { it.interviewDate.isNotBlank() }
        .filter { job -> job.interviewExpiryMillis()?.let { it > now } ?: true }
        .sortedWith(
            compareBy<JobApplication> { it.interviewDate }
                .thenBy { job -> parseMinutesOfDay(job.interviewTime) ?: Int.MAX_VALUE }
        )
