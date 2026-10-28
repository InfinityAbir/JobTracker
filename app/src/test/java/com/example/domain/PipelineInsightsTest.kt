package com.example.domain

import com.example.domain.analytics.PipelineInsights
import com.example.domain.analytics.StaleApplications
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class PipelineInsightsTest {

    private fun millis(dateTime: String): Long =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(dateTime)!!.time

    private val now = millis("2026-10-28 12:00")

    private fun job(
        id: Long,
        applied: String,
        status: JobStatus = JobStatus.APPLIED,
        source: String = "",
        interviewingAt: Long = 0,
        interviewDate: String = "",
        lastFollowUpAt: Long = 0
    ) = JobApplication(
        id = id,
        companyName = "Co$id",
        jobTitle = "Dev",
        dateApplied = applied,
        status = status,
        referralSource = source,
        interviewingAt = interviewingAt,
        interviewDate = interviewDate,
        lastFollowUpAt = lastFollowUpAt,
        createdAt = now
    )

    @Test
    fun emptyList_isEmpty() {
        val result = PipelineInsights.analyze(emptyList(), now)
        assertTrue(result.isEmpty)
        assertNull(result.avgDaysToInterview)
    }

    @Test
    fun velocity_countsLast7And30Days_andIgnoresFutureAndInvalidDates() {
        val jobs = listOf(
            job(1, "2026-10-28"), // today
            job(2, "2026-10-22"), // 6 days ago: inside 7-day window
            job(3, "2026-10-21"), // 7 days ago: outside the 7-day window
            job(4, "2026-09-29"), // 29 days ago: inside the 30-day window
            job(5, "2026-09-28"), // 30 days ago: outside
            job(6, "2026-11-05"), // in the future
            job(7, "not a date")
        )
        val result = PipelineInsights.analyze(jobs, now)
        assertEquals(7, result.total)
        assertEquals(2, result.last7Days)
        assertEquals(4, result.last30Days)
    }

    @Test
    fun conversionRates_countRejectedAfterInterviewAsInterviewed() {
        val jobs = listOf(
            job(1, "2026-10-01", JobStatus.APPLIED),
            job(2, "2026-10-01", JobStatus.INTERVIEWING),
            job(3, "2026-10-01", JobStatus.OFFER),
            job(4, "2026-10-01", JobStatus.REJECTED), // rejected without ever interviewing
            job(5, "2026-10-01", JobStatus.REJECTED, interviewDate = "2026-10-10") // interviewed, then rejected
        )
        val result = PipelineInsights.analyze(jobs, now)
        assertEquals(60, result.interviewRatePercent) // 3 of 5
        assertEquals(80, result.responseRatePercent) // everything except the one still Applied
        assertEquals(20, result.offerRatePercent)
    }

    @Test
    fun avgDaysToInterview_usesOnlyRecordsWithATimestamp() {
        val jobs = listOf(
            job(1, "2026-10-01", JobStatus.INTERVIEWING, interviewingAt = millis("2026-10-05 09:00")), // 4 days
            job(2, "2026-10-10", JobStatus.OFFER, interviewingAt = millis("2026-10-13 18:00")), // 3 days
            job(3, "2026-10-01", JobStatus.INTERVIEWING) // legacy record, unknown timing
        )
        val result = PipelineInsights.analyze(jobs, now)
        assertEquals(3.5, result.avgDaysToInterview!!, 0.001)
        assertEquals(2, result.avgDaysSampleSize)
    }

    @Test
    fun avgDaysToInterview_isNullWithoutData() {
        val result = PipelineInsights.analyze(listOf(job(1, "2026-10-01", JobStatus.INTERVIEWING)), now)
        assertNull(result.avgDaysToInterview)
        assertEquals(0, result.avgDaysSampleSize)
    }

    @Test
    fun sources_areRankedByInterviewRateThenVolume() {
        val jobs = listOf(
            job(1, "2026-10-01", JobStatus.INTERVIEWING, source = "LinkedIn"),
            job(2, "2026-10-01", JobStatus.APPLIED, source = "LinkedIn"),
            job(3, "2026-10-01", JobStatus.OFFER, source = "Employee Referral"),
            job(4, "2026-10-01", JobStatus.APPLIED, source = "Job Board"),
            job(5, "2026-10-01", JobStatus.APPLIED)
        )
        val sources = PipelineInsights.analyze(jobs, now).sources
        assertEquals(listOf("Employee Referral", "LinkedIn", "", "Job Board"), sources.map { it.source })
        assertEquals(100, sources[0].interviewRatePercent)
        assertEquals(50, sources[1].interviewRatePercent)
        assertEquals(2, sources[1].applications)
    }

    // ---- stale nudges ----

    @Test
    fun stale_appliedForTwoWeeksOrMore() {
        assertEquals(14, StaleApplications.daysWithoutUpdate(job(1, "2026-10-14"), now))
        assertEquals(30, StaleApplications.daysWithoutUpdate(job(2, "2026-09-28"), now))
    }

    @Test
    fun stale_notYetAtThreshold() {
        assertNull(StaleApplications.daysWithoutUpdate(job(1, "2026-10-15"), now)) // 13 days
    }

    @Test
    fun stale_onlyForAppliedStatus() {
        for (status in listOf(JobStatus.INTERVIEWING, JobStatus.OFFER, JobStatus.REJECTED)) {
            assertNull(StaleApplications.daysWithoutUpdate(job(1, "2026-08-01", status), now))
        }
    }

    @Test
    fun stale_followUpRestartsTheClock() {
        val old = job(1, "2026-09-01")
        assertNotNull(StaleApplications.daysWithoutUpdate(old, now))
        val followedUp = old.copy(lastFollowUpAt = millis("2026-10-20 10:00"))
        assertNull(StaleApplications.daysWithoutUpdate(followedUp, now))
        // ...and it becomes stale again two weeks after the follow-up
        assertEquals(14, StaleApplications.daysWithoutUpdate(followedUp, millis("2026-11-03 12:00")))
    }

    @Test
    fun stale_customThreshold() {
        assertEquals(10, StaleApplications.daysWithoutUpdate(job(1, "2026-10-18"), now, thresholdDays = 10))
    }
}
