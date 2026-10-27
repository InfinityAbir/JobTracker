package com.example.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.JobApplication
import com.example.domain.model.JobSortOption
import com.example.domain.model.JobStatus
import com.example.domain.usecase.DeleteJobApplicationUseCase
import com.example.domain.usecase.GetJobApplicationsUseCase
import com.example.domain.usecase.GetStatusSummaryUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase
import com.example.localization.AppStrings
import com.example.util.JobDraft
import com.example.util.JobDraftManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class FilterState(
    val query: String = "",
    val status: JobStatus? = null,
    val isDraftFilter: Boolean = false,
    val sortOption: JobSortOption = JobSortOption.DATE_DESC
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val getJobApplicationsUseCase: GetJobApplicationsUseCase,
    private val getStatusSummaryUseCase: GetStatusSummaryUseCase,
    private val deleteJobApplicationUseCase: DeleteJobApplicationUseCase,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase,
    private val context: Context? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow<JobStatus?>(null)
    private val _isDraftFilter = MutableStateFlow(false)
    private val _sortOption = MutableStateFlow(JobSortOption.DATE_DESC)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _activeDraft = MutableStateFlow<JobDraft?>(loadCurrentDraft())
    private var recentlyDeletedJob: JobApplication? = null

    private fun loadCurrentDraft(): JobDraft? {
        return context?.let { JobDraftManager.getDraft(it) }
    }

    private val _filterState = combine(
        _searchQuery,
        _selectedStatus,
        _isDraftFilter,
        _sortOption
    ) { query, status, isDraft, sort ->
        FilterState(query, status, isDraft, sort)
    }

    private val _draftAndMessage = combine(_activeDraft, _userMessage) { draft, message ->
        Pair(draft, message)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        _filterState.flatMapLatest { filter ->
            getJobApplicationsUseCase(
                searchQuery = filter.query,
                statusFilter = if (filter.isDraftFilter) null else filter.status,
                sortOption = filter.sortOption
            )
        },
        getJobApplicationsUseCase(searchQuery = "", statusFilter = null, sortOption = JobSortOption.DATE_DESC),
        getStatusSummaryUseCase(),
        _filterState,
        _draftAndMessage
    ) { apps, allApps, summary, filter, draftAndMsg ->
        val (draft, message) = draftAndMsg
        val upcoming = allApps
            .filter { it.interviewDate.isNotBlank() }
            .sortedBy { it.interviewDate }

        // If draft filter is active, only non-draft apps are hidden from the normal list so the Draft section takes focus
        val displayedApps = if (filter.isDraftFilter) emptyList() else apps

        val draftCount = if (draft != null && draft.isNotEmpty()) 1 else 0
        val updatedSummary = summary.copy(drafts = draftCount)

        DashboardUiState(
            applications = displayedApps,
            rawApplications = allApps,
            upcomingInterviews = upcoming,
            summary = updatedSummary,
            activeDraft = draft,
            isDraftFilterSelected = filter.isDraftFilter,
            searchQuery = filter.query,
            selectedStatus = filter.status,
            sortOption = filter.sortOption,
            isLoading = false,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }
            is DashboardUiEvent.OnStatusFilterSelected -> {
                _isDraftFilter.value = false
                _selectedStatus.value = event.status
            }
            is DashboardUiEvent.OnSortOptionSelected -> {
                _sortOption.value = event.option
            }
            is DashboardUiEvent.OnDraftFilterToggled -> {
                if (_isDraftFilter.value) {
                    _isDraftFilter.value = false
                } else {
                    _isDraftFilter.value = true
                    _selectedStatus.value = null
                }
            }
            is DashboardUiEvent.OnRefreshDraft -> {
                _activeDraft.value = loadCurrentDraft()
            }
            is DashboardUiEvent.OnDiscardDraft -> {
                context?.let { JobDraftManager.clearDraft(it) }
                _activeDraft.value = null
                if (_isDraftFilter.value) {
                    _isDraftFilter.value = false
                }
                _userMessage.value = AppStrings.current().msgDraftDiscarded
            }
            is DashboardUiEvent.OnDeleteApplication -> {
                recentlyDeletedJob = event.application
                viewModelScope.launch {
                    deleteJobApplicationUseCase(event.application)
                    _userMessage.value = AppStrings.current().msgApplicationDeleted(event.application.companyName)
                }
            }
            is DashboardUiEvent.OnRestoreApplication -> {
                val jobToRestore = if (event.application.id > 0 && event.application.companyName.isNotBlank()) {
                    event.application
                } else {
                    recentlyDeletedJob
                }
                jobToRestore?.let { job ->
                    viewModelScope.launch {
                        saveJobApplicationUseCase(job.copy(id = 0))
                        recentlyDeletedJob = null
                        _userMessage.value = AppStrings.current().msgApplicationRestored
                    }
                }
            }
            DashboardUiEvent.OnDismissMessage -> {
                _userMessage.value = null
            }
        }
    }
}
