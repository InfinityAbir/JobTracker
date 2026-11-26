package com.example.presentation.dashboard

import com.example.domain.model.JobApplication
import com.example.domain.model.JobSortOption
import com.example.domain.model.JobStatus
import com.example.domain.model.StatusSummary
import com.example.util.JobDraft

data class DashboardUiState(
    val applications: List<JobApplication> = emptyList(),
    val rawApplications: List<JobApplication> = emptyList(),
    val upcomingInterviews: List<JobApplication> = emptyList(),
    /** Applications in the trash, most recently deleted first. */
    val trashedApplications: List<JobApplication> = emptyList(),
    /** True while the last delete can still be undone from the snackbar. */
    val canUndoDelete: Boolean = false,
    val summary: StatusSummary = StatusSummary(),
    val activeDraft: JobDraft? = null,
    val isDraftFilterSelected: Boolean = false,
    val searchQuery: String = "",
    val selectedStatus: JobStatus? = null,
    val sortOption: JobSortOption = JobSortOption.DATE_DESC,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

sealed interface DashboardUiEvent {
    data class OnSearchQueryChange(val query: String) : DashboardUiEvent
    data class OnStatusFilterSelected(val status: JobStatus?) : DashboardUiEvent
    data class OnSortOptionSelected(val option: JobSortOption) : DashboardUiEvent
    data class OnDeleteApplication(val application: JobApplication) : DashboardUiEvent
    data class OnRestoreApplication(val application: JobApplication) : DashboardUiEvent
    data class OnRestoreFromTrash(val application: JobApplication) : DashboardUiEvent
    data class OnDeleteForever(val application: JobApplication) : DashboardUiEvent
    data object OnEmptyTrash : DashboardUiEvent
    data object OnDraftFilterToggled : DashboardUiEvent
    data object OnDiscardDraft : DashboardUiEvent
    data object OnRefreshDraft : DashboardUiEvent
    data object OnDismissMessage : DashboardUiEvent
}
