package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object JobExportHelper {

    fun generateCsv(applications: List<JobApplication>): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("ID,Company,Job Title,Status,Date Applied,Salary,Interview Date,Interview Type,Recruiter Name,Recruiter Email,Recruiter Phone,Referral Source,Notes,Interview Notes\n")

        for (job in applications) {
            sb.append(escapeCsv(job.id.toString())).append(",")
            sb.append(escapeCsv(job.companyName)).append(",")
            sb.append(escapeCsv(job.jobTitle)).append(",")
            sb.append(escapeCsv(job.status.displayName)).append(",")
            sb.append(escapeCsv(job.dateApplied)).append(",")
            sb.append(escapeCsv(job.salary)).append(",")
            sb.append(escapeCsv(job.interviewDate)).append(",")
            sb.append(escapeCsv(job.interviewType)).append(",")
            sb.append(escapeCsv(job.recruiterName)).append(",")
            sb.append(escapeCsv(job.recruiterEmail)).append(",")
            sb.append(escapeCsv(job.recruiterPhone)).append(",")
            sb.append(escapeCsv(job.referralSource)).append(",")
            sb.append(escapeCsv(job.notes)).append(",")
            sb.append(escapeCsv(job.interviewNotes))
            sb.append("\n")
        }
        return sb.toString()
    }

    private val phoneLike = Regex("""^\+[\d\s()-]+$""")

    private fun escapeCsv(value: String): String {
        var formatted = value
        // Spreadsheet formula injection: cells may hold text pasted from untrusted job posts.
        if (formatted.isNotEmpty() && formatted[0] in "=+-@\t\r" && !phoneLike.matches(formatted)) {
            formatted = "'$formatted"
        }
        if (formatted.contains("\"") || formatted.contains(",") || formatted.contains("\n") || formatted.contains("\r")) {
            formatted = formatted.replace("\"", "\"\"")
            return "\"$formatted\""
        }
        return formatted
    }

    fun generateJson(applications: List<JobApplication>): String {
        val root = JSONObject()
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("totalApplications", applications.size)

        val jsonArray = JSONArray()
        for (job in applications) {
            val item = JSONObject()
            item.put("id", job.id)
            item.put("companyName", job.companyName)
            item.put("jobTitle", job.jobTitle)
            item.put("status", job.status.name)
            item.put("dateApplied", job.dateApplied)
            item.put("salary", job.salary)
            item.put("interviewDate", job.interviewDate)
            item.put("interviewType", job.interviewType)
            item.put("interviewNotes", job.interviewNotes)
            item.put("recruiterName", job.recruiterName)
            item.put("recruiterEmail", job.recruiterEmail)
            item.put("recruiterPhone", job.recruiterPhone)
            item.put("referralSource", job.referralSource)
            item.put("notes", job.notes)
            item.put("createdAt", job.createdAt)
            jsonArray.put(item)
        }
        root.put("applications", jsonArray)
        return root.toString(2)
    }

    fun generateSummaryMarkdown(applications: List<JobApplication>): String {
        val sb = StringBuilder()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        sb.append("📋 Job Applications Summary Report\n")
        sb.append("Generated on: $today\n")
        sb.append("Total Tracked Applications: ${applications.size}\n\n")

        val applied = applications.filter { it.status == JobStatus.APPLIED }
        val interviewing = applications.filter { it.status == JobStatus.INTERVIEWING }
        val offers = applications.filter { it.status == JobStatus.OFFER }
        val rejected = applications.filter { it.status == JobStatus.REJECTED }

        val upcomingInterviews = applications.filter { it.interviewDate.isNotBlank() }
            .sortedBy { it.interviewDate }

        if (upcomingInterviews.isNotEmpty()) {
            sb.append("--- 📅 UPCOMING INTERVIEWS ---\n")
            for (job in upcomingInterviews) {
                sb.append("• ${job.companyName} - ${job.jobTitle}\n")
                sb.append("  Date: ${job.interviewDate}")
                if (job.interviewType.isNotBlank()) sb.append(" (${job.interviewType})")
                if (job.interviewNotes.isNotBlank()) sb.append(" | Notes: ${job.interviewNotes}")
                sb.append("\n")
            }
            sb.append("\n")
        }

        if (offers.isNotEmpty()) {
            sb.append("--- 🎉 OFFERS (${offers.size}) ---\n")
            for (job in offers) {
                sb.append("• ${job.companyName} — ${job.jobTitle}")
                if (job.salary.isNotBlank()) sb.append(" (${job.salary})")
                sb.append("\n")
            }
            sb.append("\n")
        }

        if (interviewing.isNotEmpty()) {
            sb.append("--- ⏳ IN INTERVIEW PIPELINE (${interviewing.size}) ---\n")
            for (job in interviewing) {
                sb.append("• ${job.companyName} — ${job.jobTitle} (Applied: ${job.dateApplied})")
                if (job.recruiterName.isNotBlank()) sb.append(" | Contact: ${job.recruiterName}")
                sb.append("\n")
            }
            sb.append("\n")
        }

        if (applied.isNotEmpty()) {
            sb.append("--- 📨 APPLIED (${applied.size}) ---\n")
            for (job in applied) {
                sb.append("• ${job.companyName} — ${job.jobTitle} (Applied: ${job.dateApplied})\n")
            }
            sb.append("\n")
        }

        if (rejected.isNotEmpty()) {
            sb.append("--- 📁 ARCHIVED / REJECTED (${rejected.size}) ---\n")
            for (job in rejected) {
                sb.append("• ${job.companyName} — ${job.jobTitle}\n")
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    fun shareContent(context: Context, content: String, title: String, mimeType: String = "text/plain") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = mimeType
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Job Tracker Export"): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addInterviewToCalendar(context: Context, application: JobApplication) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Interview: ${application.companyName} - ${application.jobTitle}")
                val description = buildString {
                    if (application.interviewType.isNotBlank()) append("Type: ${application.interviewType}\n")
                    if (application.recruiterName.isNotBlank()) append("Recruiter: ${application.recruiterName}\n")
                    if (application.recruiterEmail.isNotBlank()) append("Email: ${application.recruiterEmail}\n")
                    if (application.interviewNotes.isNotBlank()) append("Notes/Link: ${application.interviewNotes}\n")
                    if (application.notes.isNotBlank()) append("\nRole Notes:\n${application.notes}")
                }
                putExtra(CalendarContract.Events.DESCRIPTION, description)

                // Attempt to parse date
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = try {
                    if (application.interviewDate.isNotBlank()) {
                        sdf.parse(application.interviewDate.take(10))
                    } else null
                } catch (e: Exception) {
                    null
                }

                if (parsedDate != null) {
                    val cal = Calendar.getInstance().apply {
                        time = parsedDate
                        set(Calendar.HOUR_OF_DAY, 10)
                        set(Calendar.MINUTE, 0)
                    }
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 60 * 60 * 1000)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: share content or ignore if calendar app unavailable
        }
    }
}
