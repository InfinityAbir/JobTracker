package com.example.localization

import androidx.compose.runtime.compositionLocalOf
import com.example.domain.model.JobSortOption
import com.example.domain.model.JobStatus
import com.example.util.AppLanguage

val LocalAppStrings = compositionLocalOf<AppStrings> { AppStrings.English }

sealed class AppStrings {
    // App & Header
    abstract val appName: String
    abstract val appSubtitle: String
    abstract val languageToggleDesc: String
    abstract val undo: String
    abstract val discard: String

    // Dashboard
    abstract val exportAndBackup: String
    abstract val aiTools: String
    abstract val addApplication: String
    abstract val pipelineTitle: String
    abstract fun totalCount(count: Int): String
    abstract val statusAll: String
    abstract val statusDraft: String
    abstract val statusApplied: String
    abstract val statusInterviewing: String
    abstract val statusOffer: String
    abstract val statusRejected: String
    abstract val draftActive: String
    abstract fun selectedStatusIndicator(name: String): String

    // Draft Section
    abstract val inProgressDraft: String
    abstract val filtered: String
    abstract val savedJustNow: String
    abstract fun savedMinutesAgo(m: Long): String
    abstract fun savedHoursAgo(h: Long): String
    abstract fun savedDaysAgo(d: Long): String
    abstract fun savedOnDate(date: String): String
    abstract val resumeEditDraft: String
    abstract val discardDraft: String
    abstract val discardDraftDialogTitle: String
    abstract fun discardDraftDialogMessage(company: String): String
    abstract val keepDraft: String
    abstract val draftRestoredTitle: String
    abstract val draftRestoredSubtitle: String

    // Upcoming Interviews
    abstract val upcomingInterviews: String
    abstract fun scheduledCount(count: Int): String
    abstract val addToCalendar: String
    abstract fun alarmReminderActive(minutes: Int): String
    abstract val relativeToday: String
    abstract val relativeTomorrow: String
    abstract fun relativeInDays(days: Long): String
    abstract fun relativeInWeeks(weeks: Long): String
    abstract val relativeYesterday: String
    abstract val relativeScheduled: String

    // Search & Sort
    abstract val searchPlaceholder: String
    abstract val clearSearch: String
    abstract val sortButtonDesc: String

    // Empty State
    abstract val emptySearchTitle: String
    abstract val emptySearchSubtitle: String
    abstract val emptyNoAppsTitle: String
    abstract val emptyNoAppsSubtitle: String
    abstract val clearFilterButton: String
    abstract val emptyAddAppButton: String

    // Job Card
    abstract val deleteSwipeAction: String
    abstract val interviewBadgeLabel: String
    abstract fun interviewAlertMinutes(m: Int): String

    // Add / Edit Screen
    abstract val newApplicationTitle: String
    abstract val editApplicationTitle: String
    abstract val backButtonDesc: String
    abstract val aiAssistantDesc: String
    abstract val deleteApplicationDesc: String
    abstract val saveChanges: String
    abstract val addApplicationButton: String
    abstract val companyNameLabel: String
    abstract val companyNamePlaceholder: String
    abstract val companyNameRequiredError: String
    abstract val jobTitleLabel: String
    abstract val jobTitlePlaceholder: String
    abstract val jobTitleRequiredError: String
    abstract val applicationStatusLabel: String
    abstract val dateAppliedLabel: String
    abstract val todayChip: String
    abstract val yesterdayChip: String
    abstract val salaryLabel: String
    abstract val salaryPlaceholder: String
    abstract val interviewScheduleSection: String
    abstract val interviewDateLabel: String
    abstract val tomorrowChip: String
    abstract val in3DaysChip: String
    abstract val nextWeekChip: String
    abstract val clearChip: String
    abstract val interviewStageLabel: String
    abstract val meetingLinkLabel: String
    abstract val meetingLinkPlaceholder: String
    abstract val interviewTimeLabel: String
    abstract val interviewTimePlaceholder: String
    abstract val timedCallReminderTitle: String
    abstract val timedCallReminderSubtitle: String
    abstract val reminderOffsetLabel: String
    abstract fun minutesBefore(m: Int): String
    abstract val oneHourBefore: String
    abstract val recruiterSectionTitle: String
    abstract val recruiterNameLabel: String
    abstract val recruiterEmailLabel: String
    abstract val recruiterPhoneLabel: String
    abstract val referralSourceLabel: String
    abstract val notesSectionTitle: String
    abstract val notesPlaceholder: String
    abstract val deleteDialogTitle: String
    abstract val deleteDialogMessage: String
    abstract val deleteButton: String
    abstract val cancelButton: String
    abstract val unsavedDialogTitle: String
    abstract val unsavedDialogMessage: String
    abstract val saveDraftAndExit: String
    abstract val keepEditing: String

    // Export & Backup
    abstract val backupSheetTitle: String
    abstract fun applicationsTrackedCount(count: Int): String
    abstract val persistentSyncTitle: String
    abstract val persistentSyncDesc: String
    abstract val restoreAndSyncSection: String
    abstract val scanRestoreTitle: String
    abstract val scanRestoreSubtitle: String
    abstract val importJsonTitle: String
    abstract val importJsonSubtitle: String
    abstract val exportShareSection: String
    abstract val exportCsvTitle: String
    abstract val exportCsvSubtitle: String
    abstract val exportJsonTitle: String
    abstract val exportJsonSubtitle: String
    abstract val copySummaryTitle: String
    abstract val copySummarySubtitle: String

    // AI Tools
    abstract val aiSheetTitle: String
    abstract val aiPoweredBy: String
    abstract val clearAll: String
    abstract val tabJobParser: String
    abstract val tabCoverLetter: String
    abstract val tabFollowUp: String
    abstract val parserExplainer: String
    abstract val coverLetterExplainer: String
    abstract val followUpExplainer: String
    abstract val jobPostingLabel: String
    abstract val jobPostingPlaceholder: String
    abstract val clearInput: String
    abstract val targetRoleLabel: String
    abstract val targetRolePlaceholder: String
    abstract val candidateProfileLabel: String
    abstract val candidateProfilePlaceholder: String
    abstract val emailScenarioLabel: String
    abstract val emailNotesLabel: String
    abstract val emailNotesPlaceholder: String
    abstract val btnExtractJob: String
    abstract val btnGenerateCoverLetter: String
    abstract val btnDraftEmail: String
    abstract val aiAnalyzing: String
    abstract val retryButton: String
    abstract val generatedOutputHeader: String
    abstract val readyToAutoFillHeader: String
    abstract val autoFillDescCurrent: String
    abstract val autoFillDescNew: String
    abstract val btnFillCurrentForm: String
    abstract val btnCreateWithExtracted: String

    // Helper functions for localized enum display
    abstract fun getStatusDisplayName(status: JobStatus): String
    abstract fun getSortDisplayName(sort: JobSortOption): String
    abstract fun getInterviewStageName(stage: String): String

    // -------------------------------------------------------------
    // ENGLISH
    // -------------------------------------------------------------
    object English : AppStrings() {
        override val appName = "Job Tracker"
        override val appSubtitle = "Career Tracker & Interview Hub"
        override val languageToggleDesc = "Switch Language (English / বাংলা)"

        override val exportAndBackup = "Export & Backup"
        override val aiTools = "AI Tools"
        override val addApplication = "Add Application"
        override val pipelineTitle = "Application Pipeline"
        override fun totalCount(count: Int) = "$count Total"
        override val statusAll = "All"
        override val statusDraft = "Draft"
        override val statusApplied = "Applied"
        override val statusInterviewing = "Interviewing"
        override val statusOffer = "Offer"
        override val statusRejected = "Rejected"
        override val draftActive = "• Draft active"
        override fun selectedStatusIndicator(name: String) = "• $name"

        override val inProgressDraft = "IN-PROGRESS DRAFT"
        override val filtered = "• Filtered"
        override val savedJustNow = "Saved just now"
        override fun savedMinutesAgo(m: Long) = "Saved ${m}m ago"
        override fun savedHoursAgo(h: Long) = "Saved ${h}h ago"
        override fun savedDaysAgo(d: Long) = "Saved ${d}d ago"
        override fun savedOnDate(date: String) = "Saved $date"
        override val resumeEditDraft = "Resume & Edit Draft"
        override val discardDraft = "Discard Draft"
        override val discardDraftDialogTitle = "Discard Draft?"
        override fun discardDraftDialogMessage(company: String) =
            "This will permanently remove your draft application for \"$company\". You cannot undo this action."
        override val keepDraft = "Keep Draft"
        override val draftRestoredTitle = "Draft Restored"
        override val draftRestoredSubtitle =
            "We restored your previously unsaved application details so you can continue where you left off."

        override val upcomingInterviews = "Upcoming Interviews"
        override fun scheduledCount(count: Int) = "$count Scheduled"
        override val addToCalendar = "Add to Calendar"
        override fun alarmReminderActive(minutes: Int) = "Alarm reminder active (${minutes}m before)"
        override val relativeToday = "🔥 Today!"
        override val relativeTomorrow = "⚡ Tomorrow"
        override fun relativeInDays(days: Long) = "In $days days"
        override fun relativeInWeeks(weeks: Long) = "In $weeks weeks"
        override val relativeYesterday = "Yesterday"
        override val relativeScheduled = "Scheduled"

        override val searchPlaceholder = "Search company, title, notes..."
        override val clearSearch = "Clear Search"
        override val sortButtonDesc = "Sort Applications"

        override val emptySearchTitle = "No matches found"
        override val emptySearchSubtitle = "Try searching with another keyword or resetting the status filter."
        override val emptyNoAppsTitle = "No applications yet"
        override val emptyNoAppsSubtitle = "Stay organized and track your job search journey. Tap below to log your first application."
        override val clearFilterButton = "Clear Search & Filter"
        override val emptyAddAppButton = "Add Application"
        override val undo = "Undo"
        override val discard = "Discard"

        override val deleteSwipeAction = "Delete"
        override val interviewBadgeLabel = "Interview"
        override fun interviewAlertMinutes(m: Int) = "⏰ ${m}m alert"

        override val newApplicationTitle = "New Application"
        override val editApplicationTitle = "Edit Application"
        override val backButtonDesc = "Back"
        override val aiAssistantDesc = "AI Assistant (Job Parser, Cover Letter, Emails)"
        override val deleteApplicationDesc = "Delete Application"
        override val saveChanges = "Save Changes"
        override val addApplicationButton = "Add Application"
        override val companyNameLabel = "Company Name *"
        override val companyNamePlaceholder = "e.g. Google, Stripe, Airbnb"
        override val companyNameRequiredError = "Company name cannot be blank"
        override val jobTitleLabel = "Job Title *"
        override val jobTitlePlaceholder = "e.g. Senior Android Engineer"
        override val jobTitleRequiredError = "Job title cannot be blank"
        override val applicationStatusLabel = "Application Status"
        override val dateAppliedLabel = "Date Applied (YYYY-MM-DD)"
        override val todayChip = "Today"
        override val yesterdayChip = "Yesterday"
        override val salaryLabel = "Salary / Compensation (Optional)"
        override val salaryPlaceholder = "e.g. $140,000 - $160,000 / yr"
        override val interviewScheduleSection = "Interview Schedule & Stage"
        override val interviewDateLabel = "Interview Date (YYYY-MM-DD)"
        override val tomorrowChip = "Tomorrow"
        override val in3DaysChip = "+3 Days"
        override val nextWeekChip = "Next Week"
        override val clearChip = "Clear"
        override val interviewStageLabel = "Interview Stage"
        override val meetingLinkLabel = "Meeting Link / Call Notes"
        override val meetingLinkPlaceholder = "Google Meet/Zoom link, dial-in, interviewer names..."
        override val interviewTimeLabel = "Interview / Call Time"
        override val interviewTimePlaceholder = "e.g. 10:30 AM, 14:00"
        override val timedCallReminderTitle = "Exact Timed Call Reminder"
        override val timedCallReminderSubtitle = "Rings via AlarmManager with meeting link"
        override val reminderOffsetLabel = "Reminder Time:"
        override fun minutesBefore(m: Int) = "$m min before"
        override val oneHourBefore = "1 hr before"
        override val recruiterSectionTitle = "Recruiter & Referral Contact"
        override val recruiterNameLabel = "Recruiter / Contact Name"
        override val recruiterEmailLabel = "Recruiter Email"
        override val recruiterPhoneLabel = "Phone or LinkedIn Profile"
        override val referralSourceLabel = "Referral / Job Source"
        override val notesSectionTitle = "Notes & Description"
        override val notesPlaceholder = "Paste job description highlights, referral contacts, interview notes..."
        override val deleteDialogTitle = "Delete Application"
        override val deleteDialogMessage = "Are you sure you want to delete this job application? This action cannot be undone."
        override val deleteButton = "Delete"
        override val cancelButton = "Cancel"
        override val unsavedDialogTitle = "Unsaved Application"
        override val unsavedDialogMessage = "You have unsaved information. Would you like to save this application as a draft so you can resume later, or discard it?"
        override val saveDraftAndExit = "Save Draft & Exit"
        override val keepEditing = "Keep Editing"

        override val backupSheetTitle = "Data Backup & Local Auto-Sync"
        override fun applicationsTrackedCount(count: Int) = "$count applications currently tracked"
        override val persistentSyncTitle = "Persistent Local Storage Auto-Sync"
        override val persistentSyncDesc = "All applications are saved locally to your device storage on every change. If you uninstall and reinstall, your previous data automatically restores."
        override val restoreAndSyncSection = "RESTORE & SYNC"
        override val scanRestoreTitle = "Scan & Auto-Restore Previous Data"
        override val scanRestoreSubtitle = "Recover all previous job applications from local persistent device backup files."
        override val importJsonTitle = "Import from Backup File (JSON)"
        override val importJsonSubtitle = "Select any previously exported job_tracker_backup.json from Files/Drive."
        override val exportShareSection = "EXPORT & SHARE"
        override val exportCsvTitle = "Export to CSV (Spreadsheet)"
        override val exportCsvSubtitle = "Formatted table compatible with Google Sheets, Excel, and Notion database import."
        override val exportJsonTitle = "Export JSON Backup"
        override val exportJsonSubtitle = "Complete structured raw backup including interview rounds, recruiter contacts, and notes."
        override val copySummaryTitle = "Copy Text Summary"
        override val copySummarySubtitle = "Copy a clean, readable Markdown report of your pipeline and interviews to clipboard."

        override val aiSheetTitle = "AI Job Assistant"
        override val aiPoweredBy = "Powered by Gemini AI"
        override val clearAll = "Clear"
        override val tabJobParser = "Job Parser"
        override val tabCoverLetter = "Cover Letter"
        override val tabFollowUp = "Follow-Up"
        override val parserExplainer = "Paste job description to automatically extract company, role title, salary, and requirements."
        override val coverLetterExplainer = "Generate a customized cover letter and top talking points matching your experience."
        override val followUpExplainer = "Draft personalized post-interview thank you notes or application status follow-ups."
        override val jobPostingLabel = "Job Posting Text:"
        override val jobPostingPlaceholder = "Paste job posting description, key duties, and qualifications here..."
        override val clearInput = "Clear Input"
        override val targetRoleLabel = "Job Context & Target Role:"
        override val targetRolePlaceholder = "e.g. Lead Android Engineer at Airbnb"
        override val candidateProfileLabel = "Your Background & Core Strengths:"
        override val candidateProfilePlaceholder = "e.g. 5+ years Kotlin/Compose experience, led offline-first architecture..."
        override val emailScenarioLabel = "Email Scenario:"
        override val emailNotesLabel = "Interview Notes / Details (Optional):"
        override val emailNotesPlaceholder = "e.g. Interviewed with Sarah (Engineering Manager) about Jetpack Compose..."
        override val btnExtractJob = "Extract Job Info"
        override val btnGenerateCoverLetter = "Generate Cover Letter"
        override val btnDraftEmail = "Draft Email"
        override val aiAnalyzing = "Gemini is analyzing..."
        override val retryButton = "Retry"
        override val generatedOutputHeader = "Generated Output"
        override val readyToAutoFillHeader = "Ready to Auto-Fill Application"
        override val autoFillDescCurrent = "Apply extracted company, title & salary into current form"
        override val autoFillDescNew = "Transfer details directly into a new application"
        override val btnFillCurrentForm = "Fill Into Current Form"
        override val btnCreateWithExtracted = "Create Application with Extracted Info"

        override fun getStatusDisplayName(status: JobStatus): String = status.displayName
        override fun getSortDisplayName(sort: JobSortOption): String = sort.displayName
        override fun getInterviewStageName(stage: String): String = stage
    }

    // -------------------------------------------------------------
    // BANGLA (বাংলা)
    // -------------------------------------------------------------
    object Bangla : AppStrings() {
        override val appName = "জব ট্র্যাকার"
        override val appSubtitle = "ক্যারিয়ার ট্র্যাকার ও ইন্টারভিউ হাব"
        override val languageToggleDesc = "ভাষা পরিবর্তন (English / বাংলা)"

        override val exportAndBackup = "ব্যাকআপ ও এক্সপোর্ট"
        override val aiTools = "এআই টুলস"
        override val addApplication = "আবেদন যোগ করুন"
        override val pipelineTitle = "আবেদনের পাইপলাইন"
        override fun totalCount(count: Int) = "$count টি মোট"
        override val statusAll = "সব"
        override val statusDraft = "ড্রাফট"
        override val statusApplied = "আবেদনকৃত"
        override val statusInterviewing = "ইন্টারভিউ"
        override val statusOffer = "অফার"
        override val statusRejected = "বাতিল"
        override val draftActive = "• ড্রাফট সক্রিয়"
        override fun selectedStatusIndicator(name: String) = "• $name"

        override val inProgressDraft = "চলমান খসড়া আবেদন"
        override val filtered = "• ফিল্টার করা"
        override val savedJustNow = "এইমাত্র সংরক্ষিত"
        override fun savedMinutesAgo(m: Long) = "$m মিনিট আগে সংরক্ষিত"
        override fun savedHoursAgo(h: Long) = "$h ঘণ্টা আগে সংরক্ষিত"
        override fun savedDaysAgo(d: Long) = "$d দিন আগে সংরক্ষিত"
        override fun savedOnDate(date: String) = "সংরক্ষিত $date"
        override val resumeEditDraft = "খসড়া চালু ও সম্পাদনা করুন"
        override val discardDraft = "খসড়া মুছে ফেলুন"
        override val discardDraftDialogTitle = "খসড়া মুছে ফেলবেন?"
        override fun discardDraftDialogMessage(company: String) =
            "\"$company\" এর জন্য আপনার খসড়া আবেদনটি স্থায়ীভাবে মুছে যাবে। এটি আর ফিরিয়ে আনা যাবে না।"
        override val keepDraft = "খসড়া রাখুন"
        override val draftRestoredTitle = "খসড়া পুনরুদ্ধার হয়েছে"
        override val draftRestoredSubtitle =
            "আপনার পূর্বের অসংরক্ষিত তথ্য পুনরুদ্ধার করা হয়েছে যেন যেখান থেকে শেষ করেছিলেন সেখান থেকে শুরু করতে পারেন।"

        override val upcomingInterviews = "আসন্ন ইন্টারভিউসমূহ"
        override fun scheduledCount(count: Int) = "$count টি নির্ধারিত"
        override val addToCalendar = "ক্যালেন্ডারে যোগ করুন"
        override fun alarmReminderActive(minutes: Int) = "অ্যালার্ম রিমাইন্ডার সক্রিয় ($minutes মি. আগে)"
        override val relativeToday = "🔥 আজ!"
        override val relativeTomorrow = "⚡ আগামীকাল"
        override fun relativeInDays(days: Long) = "$days দিন পর"
        override fun relativeInWeeks(weeks: Long) = "$weeks সপ্তাহ পর"
        override val relativeYesterday = "গতকাল"
        override val relativeScheduled = "নির্ধারিত"

        override val searchPlaceholder = "কোম্পানি, পদবী বা নোট খুঁজুন..."
        override val clearSearch = "অনুসন্ধান মুছুন"
        override val sortButtonDesc = "আবেদন সাজান"

        override val emptySearchTitle = "কোনো ফলাফল পাওয়া যায়নি"
        override val emptySearchSubtitle = "অন্য কোনো শব্দ দিয়ে খুঁজুন বা স্ট্যাটাস ফিল্টার রিসেট করুন।"
        override val emptyNoAppsTitle = "এখনও কোনো আবেদন নেই"
        override val emptyNoAppsSubtitle = "আপনার চাকরি খোঁজার প্রতিটি ধাপ সংরক্ষণ করুন। নিচে ট্যাপ করে প্রথম আবেদন যোগ করুন।"
        override val clearFilterButton = "অনুসন্ধান ও ফিল্টার মুছুন"
        override val emptyAddAppButton = "আবেদন যোগ করুন"
        override val undo = "পূর্বাবস্থায় ফেরান"
        override val discard = "বাতিল"

        override val deleteSwipeAction = "মুছে ফেলুন"
        override val interviewBadgeLabel = "ইন্টারভিউ"
        override fun interviewAlertMinutes(m: Int) = "⏰ ${m} মি. এলার্ট"

        override val newApplicationTitle = "নতুন আবেদন"
        override val editApplicationTitle = "আবেদন সম্পাদনা করুন"
        override val backButtonDesc = "ফিরে যান"
        override val aiAssistantDesc = "এআই অ্যাসিস্ট্যান্ট (জব পার্সার, কভার লেটার, ইমেইল)"
        override val deleteApplicationDesc = "আবেদন মুছে ফেলুন"
        override val saveChanges = "পরিবর্তন সংরক্ষণ করুন"
        override val addApplicationButton = "আবেদন যোগ করুন"
        override val companyNameLabel = "কোম্পানির নাম *"
        override val companyNamePlaceholder = "যেমন: গুগল, বিকাশ, গ্রামীণফোন"
        override val companyNameRequiredError = "কোম্পানির নাম খালি রাখা যাবে না"
        override val jobTitleLabel = "কাজের পদবী *"
        override val jobTitlePlaceholder = "যেমন: সিনিয়র অ্যান্ড্রয়েড ডেভেলপার"
        override val jobTitleRequiredError = "কাজের পদবী খালি রাখা যাবে না"
        override val applicationStatusLabel = "আবেদনের অবস্থা"
        override val dateAppliedLabel = "আবেদনের তারিখ (YYYY-MM-DD)"
        override val todayChip = "আজ"
        override val yesterdayChip = "গতকাল"
        override val salaryLabel = "বেতন / পারিশ্রমিক (ঐচ্ছিক)"
        override val salaryPlaceholder = "যেমন: ১,৫০,০০০ টাকা / মাস বা $140k/yr"
        override val interviewScheduleSection = "ইন্টারভিউ সময়সূচী ও ধাপ"
        override val interviewDateLabel = "ইন্টারভিউয়ের তারিখ (YYYY-MM-DD)"
        override val tomorrowChip = "আগামীকাল"
        override val in3DaysChip = "+৩ দিন"
        override val nextWeekChip = "পরের সপ্তাহ"
        override val clearChip = "মুছুন"
        override val interviewStageLabel = "ইন্টারভিউয়ের ধাপ"
        override val meetingLinkLabel = "মিটিং লিংক / কলের নোট"
        override val meetingLinkPlaceholder = "গুগল মিট/জুম লিংক, ইন্টারভিউয়ারের নাম..."
        override val interviewTimeLabel = "ইন্টারভিউ / কলের সময়"
        override val interviewTimePlaceholder = "যেমন: সকাল ১০:৩০, দুপুর ২:০০"
        override val timedCallReminderTitle = "নির্দিষ্ট সময়ের কল রিমাইন্ডার"
        override val timedCallReminderSubtitle = "মিটিং লিংকসহ অ্যালার্ম বাজবে"
        override val reminderOffsetLabel = "রিমাইন্ডারের সময়:"
        override fun minutesBefore(m: Int) = "$m মিনিট আগে"
        override val oneHourBefore = "১ ঘণ্টা আগে"
        override val recruiterSectionTitle = "নিয়োগকারী ও রেফারেন্স যোগাযোগ"
        override val recruiterNameLabel = "নিয়োগকারী / যোগাযোগের নাম"
        override val recruiterEmailLabel = "নিয়োগকারীর ইমেইল"
        override val recruiterPhoneLabel = "ফোন বা লিঙ্কডইন প্রোফাইল"
        override val referralSourceLabel = "রেফারেন্স / চাকরির উৎস"
        override val notesSectionTitle = "নোট ও বিস্তারিত বিবরণ"
        override val notesPlaceholder = "চাকরির প্রধান বিবরণ, ইন্টারভিউ নোট বা প্রয়োজনীয় তথ্য লিখুন..."
        override val deleteDialogTitle = "আবেদন মুছে ফেলুন"
        override val deleteDialogMessage = "আপনি কি নিশ্চিতভাবে এই চাকরির আবেদনটি মুছে ফেলতে চান? এটি আর ফিরিয়ে আনা যাবে না।"
        override val deleteButton = "মুছে ফেলুন"
        override val cancelButton = "বাতিল"
        override val unsavedDialogTitle = "অসংরক্ষিত আবেদন"
        override val unsavedDialogMessage = "আপনার কিছু অসংরক্ষিত তথ্য রয়েছে। আপনি কি পরে চালিয়ে যাওয়ার জন্য এটি খসড়া হিসেবে রাখতে চান নাকি বর্জন করবেন?"
        override val saveDraftAndExit = "খসড়া সংরক্ষণ করে বের হন"
        override val keepEditing = "সম্পাদনা চালিয়ে যান"

        override val backupSheetTitle = "ডাটা ব্যাকআপ ও লোকাল অটো-সিঙ্ক"
        override fun applicationsTrackedCount(count: Int) = "$count টি আবেদন সংরক্ষিত আছে"
        override val persistentSyncTitle = "স্থায়ী ডিভাইসে অটো-সিঙ্ক"
        override val persistentSyncDesc = "সকল আবেদন আপনার ডিভাইসে তাৎক্ষণিকভাবে সংরক্ষিত থাকে। অ্যাপ মুছে পুনরায় ইনস্টল করলেও আগের সব তথ্য ফিরে পাবেন।"
        override val restoreAndSyncSection = "ডাটা পুনরুদ্ধার ও সিঙ্ক"
        override val scanRestoreTitle = "পূর্বের ডাটা স্ক্যান ও পুনরুদ্ধার"
        override val scanRestoreSubtitle = "ডিভাইসের অভ্যন্তরীণ ব্যাকআপ ফাইল থেকে সকল চাকরির আবেদন পুনরুদ্ধার করুন।"
        override val importJsonTitle = "ব্যাকআপ ফাইল থেকে ইমপোর্ট (JSON)"
        override val importJsonSubtitle = "ফাইলস বা ড্রাইভ থেকে পূর্বে এক্সপোর্ট করা JSON ফাইল নির্বাচন করুন।"
        override val exportShareSection = "এক্সপোর্ট ও শেয়ার"
        override val exportCsvTitle = "CSV আকারে এক্সপোর্ট (স্প্রেডশিট)"
        override val exportCsvSubtitle = "গুগল শিটস, এক্সেল বা নোশনে ব্যবহারের উপযোগী তালিকা।"
        override val exportJsonTitle = "JSON ব্যাকআপ এক্সপোর্ট"
        override val exportJsonSubtitle = "ইন্টারভিউ রাউন্ড, যোগাযোগ ও নোটসহ সম্পূর্ণ ব্যাকআপ।"
        override val copySummaryTitle = "টেক্সট সারাংশ কপি করুন"
        override val copySummarySubtitle = "পাইপলাইন ও ইন্টারভিউয়ের মার্কডাউন রিপোর্ট ক্লিপবোর্ডে কপি করুন।"

        override val aiSheetTitle = "এআই জব অ্যাসিস্ট্যান্ট"
        override val aiPoweredBy = "জেমিনি এআই চালিত"
        override val clearAll = "মুছুন"
        override val tabJobParser = "জব পার্সার"
        override val tabCoverLetter = "কভার লেটার"
        override val tabFollowUp = "ফলো-আপ"
        override val parserExplainer = "কোম্পানি, পদবী, বেতন ও যোগ্যতা বের করতে বিজ্ঞাপনের বিবরণ এখানে পেস্ট করুন।"
        override val coverLetterExplainer = "আপনার অভিজ্ঞতার সাথে মিল রেখে মানসম্মত কভার লেটার তৈরি করুন।"
        override val followUpExplainer = "ইন্টারভিউ-পরবর্তী ধন্যবাদ বার্তা বা আবেদনের বর্তমান অবস্থা জানার ইমেইল খসড়া করুন।"
        override val jobPostingLabel = "চাকরির বিজ্ঞাপনের বিবরণ:"
        override val jobPostingPlaceholder = "বিজ্ঞাপনের বিবরণ, দায়িত্ব ও যোগ্যতার অংশটুকু এখানে পেস্ট করুন..."
        override val clearInput = "ইনপুট মুছুন"
        override val targetRoleLabel = "উদ্দিষ্ট পদবী ও কোম্পানি:"
        override val targetRolePlaceholder = "যেমন: বিকাশে সিনিয়র অ্যান্ড্রয়েড ইঞ্জিনিয়ার"
        override val candidateProfileLabel = "আপনার অভিজ্ঞতা ও মূল দক্ষতা:"
        override val candidateProfilePlaceholder = "যেমন: ৫+ বছরের কোটলিন/কম্পোজ অভিজ্ঞতা, অফলাইন-ফার্স্ট আর্কিটেকচার..."
        override val emailScenarioLabel = "ইমেইলের ধরণ:"
        override val emailNotesLabel = "ইন্টারভিউয়ের নোট / বিবরণ (ঐচ্ছিক):"
        override val emailNotesPlaceholder = "যেমন: ইঞ্জিনিয়ারিং ম্যানেজারের সাথে জেটপ্যাক কম্পোজ নিয়ে ইন্টারভিউ হয়েছিল..."
        override val btnExtractJob = "চাকরির তথ্য বের করুন"
        override val btnGenerateCoverLetter = "কভার লেটার তৈরি করুন"
        override val btnDraftEmail = "ইমেইল খসড়া করুন"
        override val aiAnalyzing = "জেমিনি এআই বিশ্লেষণ করছে..."
        override val retryButton = "পুনরায় চেষ্টা করুন"
        override val generatedOutputHeader = "তৈরিকৃত ফলাফল"
        override val readyToAutoFillHeader = "আবেদনে স্বয়ংক্রিয়ভাবে বসানোর জন্য প্রস্তুত"
        override val autoFillDescCurrent = "বর্তমান ফর্মে কোম্পানি, পদবী ও বেতন বসান"
        override val autoFillDescNew = "সরাসরি নতুন আবেদনে এই তথ্য স্থানান্তর করুন"
        override val btnFillCurrentForm = "বর্তমান ফর্মে পূরণ করুন"
        override val btnCreateWithExtracted = "সংগৃহীত তথ্য দিয়ে আবেদন তৈরি করুন"

        override fun getStatusDisplayName(status: JobStatus): String = when (status) {
            JobStatus.APPLIED -> "আবেদনকৃত"
            JobStatus.INTERVIEWING -> "ইন্টারভিউ"
            JobStatus.OFFER -> "অফার"
            JobStatus.REJECTED -> "বাতিল"
        }

        override fun getSortDisplayName(sort: JobSortOption): String = when (sort) {
            JobSortOption.DATE_DESC -> "সর্বশেষ আগে"
            JobSortOption.DATE_ASC -> "সবচেয়ে পুরানো আগে"
            JobSortOption.COMPANY_ASC -> "কোম্পানি (A-Z)"
            JobSortOption.TITLE_ASC -> "কাজের পদবী (A-Z)"
        }

        override fun getInterviewStageName(stage: String): String = when (stage) {
            "Recruiter Screen" -> "রিক্রুটার স্ক্রিন"
            "Technical Round" -> "টেকনিক্যাল রাউন্ড"
            "System Design" -> "সিস্টেম ডিজাইন"
            "Hiring Manager" -> "হায়ারিং ম্যানেজার"
            "Onsite" -> "অনসাইট"
            else -> stage
        }
    }

    companion object {
        fun get(language: AppLanguage): AppStrings = when (language) {
            AppLanguage.ENGLISH -> English
            AppLanguage.BANGLA -> Bangla
        }
    }
}
