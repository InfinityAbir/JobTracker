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
    val createdAt: Long = System.currentTimeMillis(),
    val workModel: String = "",
    val offerTotalComp: String = "",
    /** Comma-separated [OfferBenefit] keys. */
    val offerBenefits: String = "",
    /** One point per line. */
    val offerPros: String = "",
    val offerCons: String = "",
    val prepQuestions: String = "",
    val prepStar: String = "",
    val prepNegotiation: String = "",
    /** Epoch millis of the first move to Interviewing/Offer; 0 = unknown (older records). */
    val interviewingAt: Long = 0,
    /** Epoch millis when a follow-up email was last drafted; 0 = never. */
    val lastFollowUpAt: Long = 0
) {
    val benefitKeys: Set<String>
        get() = offerBenefits.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}

enum class WorkModel(val key: String) {
    REMOTE("Remote"), HYBRID("Hybrid"), ONSITE("Onsite");

    companion object {
        fun fromKey(key: String): WorkModel? = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}

/** Benefits offered as a checklist when comparing offers. [key] is what gets stored. */
enum class OfferBenefit(val key: String) {
    HEALTH("Health insurance"),
    EQUITY("Equity / stock"),
    RETIREMENT("Retirement / provident fund"),
    BONUS("Annual bonus"),
    PAID_LEAVE("Generous paid leave"),
    LEARNING("Learning budget"),
    FLEXIBLE("Flexible hours")
}

data class StatusSummary(
    val total: Int = 0,
    val applied: Int = 0,
    val interviewing: Int = 0,
    val offer: Int = 0,
    val rejected: Int = 0,
    val drafts: Int = 0
)
