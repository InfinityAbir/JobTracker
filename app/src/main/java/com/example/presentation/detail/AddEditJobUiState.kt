package com.example.presentation.detail

import com.example.domain.model.JobStatus

data class AddEditJobUiState(
    val id: Long = 0,
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
    val workModel: String = "",
    val offerTotalComp: String = "",
    val offerBenefits: String = "",
    val offerPros: String = "",
    val offerCons: String = "",
    val prepQuestions: String = "",
    val prepStar: String = "",
    val prepNegotiation: String = "",
    val companyNameError: String? = null,
    val jobTitleError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val testNotificationSent: Boolean = false,
    val isDraftRestored: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    val draftSavedMessage: String? = null
)

sealed interface AddEditJobUiEvent {
    data class OnCompanyNameChange(val name: String) : AddEditJobUiEvent
    data class OnJobTitleChange(val title: String) : AddEditJobUiEvent
    data class OnDateAppliedChange(val date: String) : AddEditJobUiEvent
    data class OnStatusChange(val status: JobStatus) : AddEditJobUiEvent
    data class OnSalaryChange(val salary: String) : AddEditJobUiEvent
    data class OnNotesChange(val notes: String) : AddEditJobUiEvent
    data class OnRecruiterNameChange(val name: String) : AddEditJobUiEvent
    data class OnRecruiterEmailChange(val email: String) : AddEditJobUiEvent
    data class OnRecruiterPhoneChange(val phone: String) : AddEditJobUiEvent
    data class OnReferralSourceChange(val source: String) : AddEditJobUiEvent
    data class OnInterviewDateChange(val date: String) : AddEditJobUiEvent
    data class OnInterviewTimeChange(val time: String) : AddEditJobUiEvent
    data class OnInterviewTypeChange(val type: String) : AddEditJobUiEvent
    data class OnInterviewNotesChange(val notes: String) : AddEditJobUiEvent
    data class OnReminderToggle(val enabled: Boolean) : AddEditJobUiEvent
    data class OnReminderMinutesChange(val minutes: Int) : AddEditJobUiEvent
    data class OnWorkModelChange(val model: String) : AddEditJobUiEvent
    data class OnOfferTotalCompChange(val value: String) : AddEditJobUiEvent
    data class OnOfferBenefitToggle(val key: String) : AddEditJobUiEvent
    data class OnOfferProsChange(val value: String) : AddEditJobUiEvent
    data class OnOfferConsChange(val value: String) : AddEditJobUiEvent
    data class OnPrepQuestionsChange(val value: String) : AddEditJobUiEvent
    data class OnPrepStarChange(val value: String) : AddEditJobUiEvent
    data class OnPrepNegotiationChange(val value: String) : AddEditJobUiEvent
    data object OnTestNotificationClick : AddEditJobUiEvent
    data object OnSaveClick : AddEditJobUiEvent
    data object OnDeleteClick : AddEditJobUiEvent
    data object OnDiscardDraftClick : AddEditJobUiEvent
    data object OnSaveDraftClick : AddEditJobUiEvent
    data object OnDismissDraftBanner : AddEditJobUiEvent
    data class OnShowUnsavedDialogChange(val show: Boolean) : AddEditJobUiEvent
    data object OnDismissDraftMessage : AddEditJobUiEvent
    data class OnApplyParsedJob(
        val companyName: String,
        val jobTitle: String,
        val salary: String,
        val notes: String
    ) : AddEditJobUiEvent
}
