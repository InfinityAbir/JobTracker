package com.example.presentation.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.domain.usecase.DeleteJobApplicationUseCase
import com.example.domain.usecase.GetJobByIdUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase
import com.example.util.JobDraft
import com.example.util.JobDraftManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditJobViewModel(
    private val jobId: Long,
    private val getJobByIdUseCase: GetJobByIdUseCase,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase,
    private val deleteJobApplicationUseCase: DeleteJobApplicationUseCase,
    private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditJobUiState())
    val uiState: StateFlow<AddEditJobUiState> = _uiState.asStateFlow()

    init {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (jobId > 0) {
            loadJob(jobId)
        } else {
            // Check for existing draft if creating a new job
            val draft = context?.let { JobDraftManager.getDraft(it) }
            if (draft != null && draft.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        companyName = draft.companyName,
                        jobTitle = draft.jobTitle,
                        dateApplied = draft.dateApplied.ifBlank { today },
                        status = draft.status,
                        salary = draft.salary,
                        notes = draft.notes,
                        recruiterName = draft.recruiterName,
                        recruiterEmail = draft.recruiterEmail,
                        recruiterPhone = draft.recruiterPhone,
                        referralSource = draft.referralSource,
                        interviewDate = draft.interviewDate,
                        interviewTime = draft.interviewTime,
                        interviewType = draft.interviewType,
                        interviewNotes = draft.interviewNotes,
                        reminderSet = draft.reminderSet,
                        reminderMinutesBefore = draft.reminderMinutesBefore,
                        isDraftRestored = true
                    )
                }
            } else {
                _uiState.update { it.copy(dateApplied = today) }
            }
        }
    }

    private fun loadJob(id: Long) {
        viewModelScope.launch {
            val job = getJobByIdUseCase.getDirect(id)
            if (job != null) {
                _uiState.update {
                    it.copy(
                        id = job.id,
                        companyName = job.companyName,
                        jobTitle = job.jobTitle,
                        dateApplied = job.dateApplied,
                        status = job.status,
                        salary = job.salary,
                        notes = job.notes,
                        recruiterName = job.recruiterName,
                        recruiterEmail = job.recruiterEmail,
                        recruiterPhone = job.recruiterPhone,
                        referralSource = job.referralSource,
                        interviewDate = job.interviewDate,
                        interviewTime = job.interviewTime,
                        interviewType = job.interviewType,
                        interviewNotes = job.interviewNotes,
                        reminderSet = job.reminderSet,
                        reminderMinutesBefore = job.reminderMinutesBefore
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Job application not found") }
            }
        }
    }

    fun onEvent(event: AddEditJobUiEvent) {
        when (event) {
            is AddEditJobUiEvent.OnCompanyNameChange -> {
                _uiState.update {
                    it.copy(
                        companyName = event.name,
                        companyNameError = if (event.name.isBlank()) "Company name is required" else null
                    )
                }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnJobTitleChange -> {
                _uiState.update {
                    it.copy(
                        jobTitle = event.title,
                        jobTitleError = if (event.title.isBlank()) "Job title is required" else null
                    )
                }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnDateAppliedChange -> {
                _uiState.update { it.copy(dateApplied = event.date) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnStatusChange -> {
                _uiState.update { it.copy(status = event.status) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnSalaryChange -> {
                _uiState.update { it.copy(salary = event.salary) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnNotesChange -> {
                _uiState.update { it.copy(notes = event.notes) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnRecruiterNameChange -> {
                _uiState.update { it.copy(recruiterName = event.name) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnRecruiterEmailChange -> {
                _uiState.update { it.copy(recruiterEmail = event.email) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnRecruiterPhoneChange -> {
                _uiState.update { it.copy(recruiterPhone = event.phone) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnReferralSourceChange -> {
                _uiState.update { it.copy(referralSource = event.source) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnInterviewDateChange -> {
                _uiState.update { it.copy(interviewDate = event.date) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnInterviewTimeChange -> {
                _uiState.update { it.copy(interviewTime = event.time) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnInterviewTypeChange -> {
                _uiState.update { it.copy(interviewType = event.type) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnInterviewNotesChange -> {
                _uiState.update { it.copy(interviewNotes = event.notes) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnReminderToggle -> {
                _uiState.update { it.copy(reminderSet = event.enabled) }
                autoSaveDraftIfNew()
            }
            is AddEditJobUiEvent.OnReminderMinutesChange -> {
                _uiState.update { it.copy(reminderMinutesBefore = event.minutes) }
                autoSaveDraftIfNew()
            }
            AddEditJobUiEvent.OnTestNotificationClick -> {
                _uiState.update { it.copy(testNotificationSent = true) }
            }
            AddEditJobUiEvent.OnSaveClick -> {
                saveJob()
            }
            AddEditJobUiEvent.OnDeleteClick -> {
                deleteJob()
            }
            AddEditJobUiEvent.OnDiscardDraftClick -> {
                discardDraft()
            }
            AddEditJobUiEvent.OnSaveDraftClick -> {
                manualSaveDraft()
            }
            AddEditJobUiEvent.OnDismissDraftBanner -> {
                _uiState.update { it.copy(isDraftRestored = false) }
            }
            is AddEditJobUiEvent.OnShowUnsavedDialogChange -> {
                _uiState.update { it.copy(showUnsavedChangesDialog = event.show) }
            }
            AddEditJobUiEvent.OnDismissDraftMessage -> {
                _uiState.update { it.copy(draftSavedMessage = null) }
            }
            is AddEditJobUiEvent.OnApplyParsedJob -> {
                _uiState.update {
                    it.copy(
                        companyName = if (event.companyName.isNotBlank()) event.companyName else it.companyName,
                        companyNameError = null,
                        jobTitle = if (event.jobTitle.isNotBlank()) event.jobTitle else it.jobTitle,
                        jobTitleError = null,
                        salary = if (event.salary.isNotBlank()) event.salary else it.salary,
                        notes = if (event.notes.isNotBlank()) {
                            if (it.notes.isBlank()) event.notes else "${it.notes}\n\n${event.notes}"
                        } else it.notes,
                        draftSavedMessage = "Auto-filled from job description!"
                    )
                }
                autoSaveDraftIfNew()
            }
        }
    }

    private fun autoSaveDraftIfNew() {
        if (jobId == 0L && context != null) {
            val s = _uiState.value
            val draft = JobDraft(
                companyName = s.companyName,
                jobTitle = s.jobTitle,
                dateApplied = s.dateApplied,
                status = s.status,
                salary = s.salary,
                notes = s.notes,
                recruiterName = s.recruiterName,
                recruiterEmail = s.recruiterEmail,
                recruiterPhone = s.recruiterPhone,
                referralSource = s.referralSource,
                interviewDate = s.interviewDate,
                interviewTime = s.interviewTime,
                interviewType = s.interviewType,
                interviewNotes = s.interviewNotes,
                reminderSet = s.reminderSet,
                reminderMinutesBefore = s.reminderMinutesBefore
            )
            JobDraftManager.saveDraft(context, draft)
        }
    }

    private fun manualSaveDraft() {
        if (context != null) {
            val s = _uiState.value
            val draft = JobDraft(
                companyName = s.companyName,
                jobTitle = s.jobTitle,
                dateApplied = s.dateApplied,
                status = s.status,
                salary = s.salary,
                notes = s.notes,
                recruiterName = s.recruiterName,
                recruiterEmail = s.recruiterEmail,
                recruiterPhone = s.recruiterPhone,
                referralSource = s.referralSource,
                interviewDate = s.interviewDate,
                interviewTime = s.interviewTime,
                interviewType = s.interviewType,
                interviewNotes = s.interviewNotes,
                reminderSet = s.reminderSet,
                reminderMinutesBefore = s.reminderMinutesBefore
            )
            JobDraftManager.saveDraft(context, draft)
            _uiState.update { it.copy(draftSavedMessage = "Draft saved locally") }
        }
    }

    private fun discardDraft() {
        if (context != null) {
            JobDraftManager.clearDraft(context)
        }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _uiState.update {
            AddEditJobUiState(
                dateApplied = today,
                draftSavedMessage = "Draft discarded"
            )
        }
    }

    private fun saveJob() {
        val currentState = _uiState.value
        var hasError = false

        if (currentState.companyName.isBlank()) {
            _uiState.update { it.copy(companyNameError = "Company name is required") }
            hasError = true
        }

        if (currentState.jobTitle.isBlank()) {
            _uiState.update { it.copy(jobTitleError = "Job title is required") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val application = JobApplication(
                id = currentState.id,
                companyName = currentState.companyName.trim(),
                jobTitle = currentState.jobTitle.trim(),
                dateApplied = currentState.dateApplied.trim(),
                status = currentState.status,
                salary = currentState.salary.trim(),
                notes = currentState.notes.trim(),
                recruiterName = currentState.recruiterName.trim(),
                recruiterEmail = currentState.recruiterEmail.trim(),
                recruiterPhone = currentState.recruiterPhone.trim(),
                referralSource = currentState.referralSource.trim(),
                interviewDate = currentState.interviewDate.trim(),
                interviewTime = currentState.interviewTime.trim(),
                interviewType = currentState.interviewType.trim(),
                interviewNotes = currentState.interviewNotes.trim(),
                reminderSet = currentState.reminderSet,
                reminderMinutesBefore = currentState.reminderMinutesBefore
            )

            val result = saveJobApplicationUseCase(application)
            if (result.isSuccess) {
                // Clear draft on successful application save
                if (jobId == 0L && context != null) {
                    JobDraftManager.clearDraft(context)
                }
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to save application"
                    )
                }
            }
        }
    }

    private fun deleteJob() {
        if (_uiState.value.id > 0) {
            viewModelScope.launch {
                deleteJobApplicationUseCase(_uiState.value.id)
                _uiState.update { it.copy(isDeleted = true) }
            }
        }
    }
}

