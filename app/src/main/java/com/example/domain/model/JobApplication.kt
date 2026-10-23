package com.example.domain.model

enum class JobStatus(val displayName: String) {
    APPLIED("Applied"),
    INTERVIEWING("Interviewing"),
    OFFER("Offer"),
    REJECTED("Rejected");

    companion object {
        fun fromString(value: String): JobStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: APPLIED
        }
    }
}

enum class JobSortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    COMPANY_ASC("Company (A-Z)"),
    TITLE_ASC("Job Title (A-Z)")
}

data class JobApplication(
    val id: Long = 0,
    val companyName: String,
    val jobTitle: String,
    val dateApplied: String,
    val status: JobStatus = JobStatus.APPLIED,
    val notes: String = "",
    val salary: String = "",
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
    val createdAt: Long = System.currentTimeMillis()
)

data class StatusSummary(
    val total: Int = 0,
    val applied: Int = 0,
    val interviewing: Int = 0,
    val offer: Int = 0,
    val rejected: Int = 0,
    val drafts: Int = 0
)
