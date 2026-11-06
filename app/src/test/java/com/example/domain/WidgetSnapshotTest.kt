package com.example.domain

import com.example.domain.analytics.InterviewCountdown
import com.example.domain.analytics.WidgetSnapshot
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class WidgetSnapshotTest {

    private fun at(day: Int, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun job(
        id: Long,
        status: JobStatus = JobStatus.APPLIED,
        date: String = "",
        time: String = "",
        applied: String = "2026-11-05"
    ) = JobApplication(
        id = id,
        companyName = "Co$id",
        jobTitle = "Role",
        dateApplied = applied,
        status = status,
        interviewDate = date,
        interviewTime = time
    )

    @Test
    fun countsEachStatus() {
        val snapshot = WidgetSnapshot.from(
            listOf(
                job(1), job(2),
                job(3, JobStatus.INTERVIEWING),
                job(4, JobStatus.OFFER),
                job(5, JobStatus.REJECTED)
            ),
            at(6, 9, 0)
        )
        assertEquals(2, snapshot.applied)
        assertEquals(1, snapshot.interviewing)
        assertEquals(1, snapshot.offers)
    }

    @Test
    fun noInterviews_meansNoNextAndNoCountdown() {
        val snapshot = WidgetSnapshot.from(listOf(job(1)), at(6, 9, 0))
        assertNull(snapshot.nextInterview)
        assertNull(snapshot.countdown)
        assertNull(snapshot.nextChangeAtMillis)
    }

    @Test
    fun nextInterview_skipsPastOnesAndPicksSoonest() {
        val jobs = listOf(
            job(1, JobStatus.INTERVIEWING, "2026-11-06", "10:30 AM"),
            job(2, JobStatus.INTERVIEWING, "2026-11-09", "09:00 AM"),
            job(3, JobStatus.INTERVIEWING, "2026-11-07", "09:00 AM")
        )
        assertEquals(3L, WidgetSnapshot.from(jobs, at(6, 15, 0)).nextInterview?.id)
    }

    @Test
    fun countdown_laterToday_isInMinutes() {
        val jobs = listOf(job(1, JobStatus.INTERVIEWING, "2026-11-06", "10:30 AM"))
        val snapshot = WidgetSnapshot.from(jobs, at(6, 8, 15))
        assertEquals(InterviewCountdown.InMinutes(135), snapshot.countdown)
    }

    @Test
    fun countdown_justStarted_isNow() {
        val jobs = listOf(job(1, JobStatus.INTERVIEWING, "2026-11-06", "10:30 AM"))
        assertEquals(InterviewCountdown.Now, WidgetSnapshot.from(jobs, at(6, 10, 45)).countdown)
    }

    @Test
    fun countdown_tomorrowAndLater() {
        val jobs = listOf(job(1, JobStatus.INTERVIEWING, "2026-11-07", "09:00 AM"))
        assertEquals(InterviewCountdown.Tomorrow, WidgetSnapshot.from(jobs, at(6, 22, 0)).countdown)
        assertEquals(InterviewCountdown.InDays(3), WidgetSnapshot.from(jobs, at(4, 22, 0)).countdown)
    }

    @Test
    fun countdown_noTimeToday_isToday() {
        val jobs = listOf(job(1, JobStatus.INTERVIEWING, "2026-11-06"))
        assertEquals(InterviewCountdown.Today, WidgetSnapshot.from(jobs, at(6, 14, 0)).countdown)
    }

    @Test
    fun nextChange_isWhenTheNextInterviewEnds() {
        val jobs = listOf(job(1, JobStatus.INTERVIEWING, "2026-11-06", "10:30 AM"))
        assertEquals(at(6, 11, 30), WidgetSnapshot.from(jobs, at(6, 8, 0)).nextChangeAtMillis)
    }

    @Test
    fun followUps_countOnlyStaleAppliedApplications() {
        val jobs = listOf(
            job(1, applied = "2026-10-01"),
            job(2, applied = "2026-11-04"),
            job(3, JobStatus.INTERVIEWING, applied = "2026-10-01")
        )
        assertEquals(1, WidgetSnapshot.from(jobs, at(6, 9, 0)).followUpsDue)
    }
}
