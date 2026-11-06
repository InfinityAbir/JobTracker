package com.example.domain

import com.example.domain.analytics.upcomingInterviews
import com.example.domain.model.JobApplication
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class UpcomingInterviewsTest {

    private fun at(day: Int, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun job(id: Long, date: String, time: String = "") = JobApplication(
        id = id,
        companyName = "Co$id",
        jobTitle = "Role",
        dateApplied = "2026-11-01",
        interviewDate = date,
        interviewTime = time
    )

    private fun ids(jobs: List<JobApplication>, now: Long) =
        jobs.upcomingInterviews(now).map { it.id }

    @Test
    fun interviewBeforeNow_isRemoved() {
        assertEquals(emptyList<Long>(), ids(listOf(job(1, "2026-11-06", "10:30 AM")), at(6, 12, 0)))
    }

    @Test
    fun interviewLaterToday_isKept() {
        assertEquals(listOf(1L), ids(listOf(job(1, "2026-11-06", "10:30 AM")), at(6, 9, 0)))
    }

    @Test
    fun interviewJustStarted_isKeptForGracePeriodThenRemoved() {
        val jobs = listOf(job(1, "2026-11-06", "10:30 AM"))
        assertEquals(listOf(1L), ids(jobs, at(6, 11, 15)))
        assertEquals(emptyList<Long>(), ids(jobs, at(6, 11, 30)))
    }

    @Test
    fun pmTimes_areNotReadAsMorning() {
        val jobs = listOf(job(1, "2026-11-06", "02:30 PM"))
        assertEquals(listOf(1L), ids(jobs, at(6, 12, 0)))
        assertEquals(emptyList<Long>(), ids(jobs, at(6, 16, 0)))
    }

    @Test
    fun twentyFourHourTime_isUnderstood() {
        val jobs = listOf(job(1, "2026-11-06", "14:30"))
        assertEquals(listOf(1L), ids(jobs, at(6, 12, 0)))
        assertEquals(emptyList<Long>(), ids(jobs, at(6, 16, 0)))
    }

    @Test
    fun noTime_isKeptUntilEndOfDay() {
        val jobs = listOf(job(1, "2026-11-06"))
        assertEquals(listOf(1L), ids(jobs, at(6, 23, 30)))
        assertEquals(emptyList<Long>(), ids(jobs, at(7, 0, 5)))
    }

    @Test
    fun pastDates_areRemovedAndFutureDatesKeptInOrder() {
        val jobs = listOf(
            job(1, "2026-11-10", "09:00 AM"),
            job(2, "2026-11-01", "09:00 AM"),
            job(3, "2026-11-08", "03:00 PM"),
            job(4, "2026-11-08", "09:00 AM")
        )
        assertEquals(listOf(4L, 3L, 1L), ids(jobs, at(6, 12, 0)))
    }

    @Test
    fun blankDate_isIgnored_unreadableDate_isKept() {
        val jobs = listOf(job(1, ""), job(2, "soon"))
        assertEquals(listOf(2L), ids(jobs, at(6, 12, 0)))
    }
}
