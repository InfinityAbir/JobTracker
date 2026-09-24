package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.JobStatus
import org.json.JSONObject

data class JobDraft(
    val companyName: String = "",
    val jobTitle: String = "",
    val dateApplied: String = "",
    val status: JobStatus = JobStatus.APPLIED,
    val salary: String = "",
    val notes: String = "",
    val recruiterName: String = "",
    val recruiterEmail: String = "",
    val recruiterPhone: String = "",
    val referralSource: String = "",
    val interviewDate: String = "",
    val interviewTime: String = "",
    val interviewType: String = "",
    val interviewNotes: String = "",
    val reminderSet: Boolean = false,
    val reminderMinutesBefore: Int = 15,
    val savedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun isNotEmpty(): Boolean {
        return companyName.isNotBlank() ||
                jobTitle.isNotBlank() ||
                salary.isNotBlank() ||
                notes.isNotBlank() ||
                recruiterName.isNotBlank() ||
                recruiterEmail.isNotBlank() ||
                recruiterPhone.isNotBlank() ||
                referralSource.isNotBlank() ||
                interviewDate.isNotBlank() ||
                interviewNotes.isNotBlank()
    }
}

object JobDraftManager {
    private const val PREFS_NAME = "job_tracker_drafts"
    private const val KEY_NEW_JOB_DRAFT = "key_new_job_draft"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveDraft(context: Context, draft: JobDraft) {
        if (!draft.isNotEmpty()) {
            clearDraft(context)
            return
        }

        try {
            val json = JSONObject().apply {
                put("companyName", draft.companyName)
                put("jobTitle", draft.jobTitle)
                put("dateApplied", draft.dateApplied)
                put("status", draft.status.name)
                put("salary", draft.salary)
                put("notes", draft.notes)
                put("recruiterName", draft.recruiterName)
                put("recruiterEmail", draft.recruiterEmail)
                put("recruiterPhone", draft.recruiterPhone)
                put("referralSource", draft.referralSource)
                put("interviewDate", draft.interviewDate)
                put("interviewTime", draft.interviewTime)
                put("interviewType", draft.interviewType)
                put("interviewNotes", draft.interviewNotes)
                put("reminderSet", draft.reminderSet)
                put("reminderMinutesBefore", draft.reminderMinutesBefore)
                put("savedAtTimestamp", System.currentTimeMillis())
            }
            getPrefs(context).edit().putString(KEY_NEW_JOB_DRAFT, json.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDraft(context: Context): JobDraft? {
        val rawJson = getPrefs(context).getString(KEY_NEW_JOB_DRAFT, null) ?: return null
        return try {
            val json = JSONObject(rawJson)
            val statusName = json.optString("status", JobStatus.APPLIED.name)
            val status = try {
                JobStatus.valueOf(statusName)
            } catch (e: Exception) {
                JobStatus.APPLIED
            }

            val draft = JobDraft(
                companyName = json.optString("companyName", ""),
                jobTitle = json.optString("jobTitle", ""),
                dateApplied = json.optString("dateApplied", ""),
                status = status,
                salary = json.optString("salary", ""),
                notes = json.optString("notes", ""),
                recruiterName = json.optString("recruiterName", ""),
                recruiterEmail = json.optString("recruiterEmail", ""),
                recruiterPhone = json.optString("recruiterPhone", ""),
                referralSource = json.optString("referralSource", ""),
                interviewDate = json.optString("interviewDate", ""),
                interviewTime = json.optString("interviewTime", ""),
                interviewType = json.optString("interviewType", ""),
                interviewNotes = json.optString("interviewNotes", ""),
                reminderSet = json.optBoolean("reminderSet", false),
                reminderMinutesBefore = json.optInt("reminderMinutesBefore", 15),
                savedAtTimestamp = json.optLong("savedAtTimestamp", System.currentTimeMillis())
            )

            if (draft.isNotEmpty()) draft else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearDraft(context: Context) {
        getPrefs(context).edit().remove(KEY_NEW_JOB_DRAFT).apply()
    }

    fun hasDraft(context: Context): Boolean {
        return getDraft(context) != null
    }
}
