package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.util.JobExportHelper
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JobExportHelperTest {

    @Test
    fun testGenerateCsv() {
        val apps = listOf(
            JobApplication(
                id = 1,
                companyName = "Google",
                jobTitle = "Android Engineer",
                dateApplied = "2026-09-20",
                status = JobStatus.INTERVIEWING,
                salary = "$180k",
                recruiterName = "Jane Doe",
                recruiterEmail = "jane@google.com",
                interviewDate = "2026-09-25",
                interviewType = "Technical Round"
            )
        )

        val csv = JobExportHelper.generateCsv(apps)
        assertTrue(csv.contains("Google"))
        assertTrue(csv.contains("Android Engineer"))
        assertTrue(csv.contains("Interviewing"))
        assertTrue(csv.contains("Jane Doe"))
        assertTrue(csv.contains("2026-09-25"))
    }

    @Test
    fun testGenerateJson() {
        val apps = listOf(
            JobApplication(
                id = 1,
                companyName = "Stripe",
                jobTitle = "Staff Engineer",
                dateApplied = "2026-09-21",
                status = JobStatus.OFFER,
                salary = "$220k",
                recruiterName = "Mark",
                referralSource = "Employee Referral"
            )
        )

        val json = JobExportHelper.generateJson(apps)
        assertTrue(json.contains("Stripe"))
        assertTrue(json.contains("Staff Engineer"))
        assertTrue(json.contains("OFFER"))
        assertTrue(json.contains("Employee Referral"))
    }

    @Test
    fun testInterviewTriggerTimeCalculation() {
        val triggerTime = com.example.util.InterviewNotificationHelper.calculateTriggerTimeMillis(
            dateStr = "2026-10-15",
            timeStr = "14:30",
            minutesBefore = 15
        )
        org.junit.Assert.assertNotNull(triggerTime)
        assertTrue(triggerTime!! > 0)
    }
}
