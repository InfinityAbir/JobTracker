package com.example.presentation

import app.cash.turbine.test
import com.example.domain.model.JobApplication
import com.example.domain.model.JobSortOption
import com.example.domain.model.JobStatus
import com.example.domain.repository.JobRepository
import com.example.domain.usecase.DeleteJobApplicationUseCase
import com.example.domain.usecase.DeleteJobPermanentlyUseCase
import com.example.domain.usecase.EmptyTrashUseCase
import com.example.domain.usecase.GetTrashedJobsUseCase
import com.example.domain.usecase.RestoreJobFromTrashUseCase
import com.example.domain.usecase.GetJobApplicationsUseCase
import com.example.domain.usecase.GetStatusSummaryUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase
import com.example.presentation.dashboard.DashboardUiEvent
import com.example.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleJobs = listOf(
        JobApplication(
            id = 1L,
            companyName = "Google",
            jobTitle = "Android Engineer",
            dateApplied = "2026-09-20",
            status = JobStatus.APPLIED
        ),
        JobApplication(
            id = 2L,
            companyName = "Amazon",
            jobTitle = "Systems Developer",
            dateApplied = "2026-09-18",
            status = JobStatus.INTERVIEWING
        ),
        JobApplication(
            id = 3L,
            companyName = "Apple",
            jobTitle = "iOS / Multiplatform Engineer",
            dateApplied = "2026-09-19",
            status = JobStatus.OFFER
        )
    )

    private val fakeRepository = FakeJobRepository(sampleJobs)
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val getAppsUseCase = GetJobApplicationsUseCase(fakeRepository)
        val getSummaryUseCase = GetStatusSummaryUseCase(fakeRepository)
        val deleteUseCase = DeleteJobApplicationUseCase(fakeRepository)
        val saveUseCase = SaveJobApplicationUseCase(fakeRepository)

        viewModel = DashboardViewModel(
            getJobApplicationsUseCase = getAppsUseCase,
            getStatusSummaryUseCase = getSummaryUseCase,
            deleteJobApplicationUseCase = deleteUseCase,
            saveJobApplicationUseCase = saveUseCase,
            getTrashedJobsUseCase = GetTrashedJobsUseCase(fakeRepository),
            restoreJobFromTrashUseCase = RestoreJobFromTrashUseCase(fakeRepository),
            deleteJobPermanentlyUseCase = DeleteJobPermanentlyUseCase(fakeRepository),
            emptyTrashUseCase = EmptyTrashUseCase(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initialEmission_containsApplicationsAndSummary() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val item = awaitItem()
            testScheduler.advanceUntilIdle()
            val state = if (item.isLoading) awaitItem() else item

            assertEquals(3, state.applications.size)
            assertEquals(3, state.summary.total)
            assertEquals(1, state.summary.applied)
            assertEquals(1, state.summary.interviewing)
            assertEquals(1, state.summary.offer)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onSearchQueryChange_filtersApplications() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // initial
            testScheduler.advanceUntilIdle()

            viewModel.onEvent(DashboardUiEvent.OnSearchQueryChange("Google"))
            testScheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(1, state.applications.size)
            assertEquals("Google", state.applications[0].companyName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onStatusFilterSelected_filtersApplications() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            testScheduler.advanceUntilIdle()

            viewModel.onEvent(DashboardUiEvent.OnStatusFilterSelected(JobStatus.INTERVIEWING))
            testScheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(1, state.applications.size)
            assertEquals("Amazon", state.applications[0].companyName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onSortOptionSelected_sortsApplicationsAlphabetically() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            testScheduler.advanceUntilIdle()

            viewModel.onEvent(DashboardUiEvent.OnSortOptionSelected(JobSortOption.COMPANY_ASC))
            testScheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals("Amazon", state.applications[0].companyName)
            assertEquals("Apple", state.applications[1].companyName)
            assertEquals("Google", state.applications[2].companyName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onDraftFilterToggled_updatesFilterState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            testScheduler.advanceUntilIdle()

            viewModel.onEvent(DashboardUiEvent.OnDraftFilterToggled)
            testScheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(true, state.isDraftFilterSelected)
            assertEquals(null, state.selectedStatus)

            viewModel.onEvent(DashboardUiEvent.OnDraftFilterToggled)
            testScheduler.advanceUntilIdle()

            val revertedState = expectMostRecentItem()
            assertEquals(false, revertedState.isDraftFilterSelected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onDeleteApplication_andRestore_restoresApplication() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            testScheduler.advanceUntilIdle()

            val initialJobs = expectMostRecentItem().applications
            val jobToDelete = initialJobs[0]

            viewModel.onEvent(DashboardUiEvent.OnDeleteApplication(jobToDelete))
            testScheduler.advanceUntilIdle()

            val stateAfterDelete = expectMostRecentItem()
            assertEquals(2, stateAfterDelete.applications.size)
            assertEquals("Application for ${jobToDelete.companyName} moved to trash", stateAfterDelete.userMessage)
            assertEquals(1, stateAfterDelete.trashedApplications.size)
            assertTrue(stateAfterDelete.canUndoDelete)

            viewModel.onEvent(DashboardUiEvent.OnRestoreApplication(jobToDelete))
            testScheduler.advanceUntilIdle()

            val stateAfterRestore = expectMostRecentItem()
            assertEquals(3, stateAfterRestore.applications.size)
            assertEquals("Application restored", stateAfterRestore.userMessage)
            assertTrue(stateAfterRestore.trashedApplications.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletedApplication_goesToTrash_andCanBeRestoredFromIt() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            testScheduler.advanceUntilIdle()
            val job = expectMostRecentItem().applications[0]

            viewModel.onEvent(DashboardUiEvent.OnDeleteApplication(job))
            testScheduler.advanceUntilIdle()
            val inTrash = expectMostRecentItem()
            assertEquals(2, inTrash.applications.size)
            assertEquals(listOf(job.id), inTrash.trashedApplications.map { it.id })
            // Trashed jobs stay out of the pipeline counts
            assertEquals(2, inTrash.summary.total)

            viewModel.onEvent(DashboardUiEvent.OnRestoreFromTrash(inTrash.trashedApplications.first()))
            testScheduler.advanceUntilIdle()
            val restored = expectMostRecentItem()
            assertEquals(3, restored.applications.size)
            assertTrue(restored.trashedApplications.isEmpty())
            // Same record comes back, not a copy with a new id
            assertTrue(restored.applications.any { it.id == job.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteForever_removesOnlyThatApplicationFromTrash() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            testScheduler.advanceUntilIdle()
            val jobs = expectMostRecentItem().applications

            viewModel.onEvent(DashboardUiEvent.OnDeleteApplication(jobs[0]))
            viewModel.onEvent(DashboardUiEvent.OnDeleteApplication(jobs[1]))
            testScheduler.advanceUntilIdle()
            assertEquals(2, expectMostRecentItem().trashedApplications.size)

            viewModel.onEvent(DashboardUiEvent.OnDeleteForever(jobs[0]))
            testScheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(listOf(jobs[1].id), state.trashedApplications.map { it.id })
            assertEquals(1, state.applications.size)
            assertEquals(false, state.canUndoDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emptyTrash_permanentlyDeletesEverythingInTrash() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()
            testScheduler.advanceUntilIdle()
            val jobs = expectMostRecentItem().applications

            viewModel.onEvent(DashboardUiEvent.OnDeleteApplication(jobs[0]))
            viewModel.onEvent(DashboardUiEvent.OnDeleteApplication(jobs[1]))
            testScheduler.advanceUntilIdle()
            assertEquals(2, expectMostRecentItem().trashedApplications.size)

            viewModel.onEvent(DashboardUiEvent.OnEmptyTrash)
            testScheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.trashedApplications.isEmpty())
            assertEquals(1, state.applications.size)
            assertEquals("Trash emptied (2 permanently deleted)", state.userMessage)
            // Nothing left to bring back, so the snackbar must not offer Undo any more
            assertEquals(false, state.canUndoDelete)
            // Gone for good: not in the repository at all
            assertEquals(1, fakeRepository.allIncludingTrashed().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

class FakeJobRepository(initialJobs: List<JobApplication> = emptyList()) : JobRepository {
    private val jobsFlow = MutableStateFlow(initialJobs)

    override fun getApplications(): Flow<List<JobApplication>> =
        jobsFlow.map { list -> list.filter { it.deletedAt == 0L } }

    override suspend fun getAllDirect(): List<JobApplication> = jobsFlow.value.filter { it.deletedAt == 0L }

    fun allIncludingTrashed(): List<JobApplication> = jobsFlow.value

    override fun getApplicationById(id: Long): Flow<JobApplication?> =
        flowOf(jobsFlow.value.firstOrNull { it.id == id })

    override suspend fun getApplicationByIdDirect(id: Long): JobApplication? =
        jobsFlow.value.firstOrNull { it.id == id }

    override suspend fun insertApplication(application: JobApplication): Long {
        val newId = (jobsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newJob = application.copy(id = newId)
        jobsFlow.value = jobsFlow.value + newJob
        return newId
    }

    override suspend fun insertApplications(applications: List<JobApplication>) {
        jobsFlow.value = jobsFlow.value + applications
    }

    override suspend fun updateApplication(application: JobApplication) {
        jobsFlow.value = jobsFlow.value.map { if (it.id == application.id) application else it }
    }

    override suspend fun deleteApplication(application: JobApplication) {
        jobsFlow.value = jobsFlow.value.filter { it.id != application.id }
    }

    override suspend fun deleteApplicationById(id: Long) {
        jobsFlow.value = jobsFlow.value.filter { it.id != id }
    }

    override fun getTrashedApplications(): Flow<List<JobApplication>> =
        jobsFlow.map { list -> list.filter { it.deletedAt > 0L }.sortedByDescending { it.deletedAt } }

    // Strictly increasing so "newest deletion first" is deterministic inside one millisecond.
    private var clock = 1_000L

    override suspend fun moveToTrash(id: Long) {
        jobsFlow.value = jobsFlow.value.map { if (it.id == id) it.copy(deletedAt = ++clock) else it }
    }

    override suspend fun restoreFromTrash(id: Long) {
        jobsFlow.value = jobsFlow.value.map { if (it.id == id) it.copy(deletedAt = 0L) else it }
    }

    override suspend fun emptyTrash(): Int {
        val removed = jobsFlow.value.count { it.deletedAt > 0L }
        jobsFlow.value = jobsFlow.value.filter { it.deletedAt == 0L }
        return removed
    }
}
