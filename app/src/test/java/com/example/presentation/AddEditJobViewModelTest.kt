package com.example.presentation

import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.domain.usecase.DeleteJobApplicationUseCase
import com.example.domain.usecase.GetJobByIdUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase
import com.example.presentation.detail.AddEditJobUiEvent
import com.example.presentation.detail.AddEditJobViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AddEditJobViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeJobRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeJobRepository(
            listOf(
                JobApplication(
                    id = 10L,
                    companyName = "Microsoft",
                    jobTitle = "Software Architect",
                    dateApplied = "2026-09-01",
                    status = JobStatus.OFFER,
                    salary = "$220,000",
                    notes = "Signed offer"
                )
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_withExistingJobId_loadsJobDetails() = runTest(testDispatcher) {
        val viewModel = AddEditJobViewModel(
            jobId = 10L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository),
            deleteJobApplicationUseCase = DeleteJobApplicationUseCase(fakeRepository)
        )

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Microsoft", state.companyName)
        assertEquals("Software Architect", state.jobTitle)
        assertEquals(JobStatus.OFFER, state.status)
        assertEquals("$220,000", state.salary)
    }

    @Test
    fun saveJob_withEmptyFields_triggersValidationErrors() = runTest(testDispatcher) {
        val viewModel = AddEditJobViewModel(
            jobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository),
            deleteJobApplicationUseCase = DeleteJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(AddEditJobUiEvent.OnSaveClick)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.companyNameError)
        assertNotNull(state.jobTitleError)
        assertEquals(false, state.isSaved)
    }

    @Test
    fun saveJob_withValidFields_savesSuccessfully() = runTest(testDispatcher) {
        val viewModel = AddEditJobViewModel(
            jobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository),
            deleteJobApplicationUseCase = DeleteJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(AddEditJobUiEvent.OnCompanyNameChange("Figma"))
        viewModel.onEvent(AddEditJobUiEvent.OnJobTitleChange("Product Designer / Engineer"))
        viewModel.onEvent(AddEditJobUiEvent.OnStatusChange(JobStatus.INTERVIEWING))
        viewModel.onEvent(AddEditJobUiEvent.OnSalaryChange("$195,000"))
        viewModel.onEvent(AddEditJobUiEvent.OnSaveClick)

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
    }

    @Test
    fun discardDraft_resetsState() = runTest(testDispatcher) {
        val viewModel = AddEditJobViewModel(
            jobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository),
            deleteJobApplicationUseCase = DeleteJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(AddEditJobUiEvent.OnCompanyNameChange("Temp Corp"))
        viewModel.onEvent(AddEditJobUiEvent.OnJobTitleChange("Developer"))
        viewModel.onEvent(AddEditJobUiEvent.OnDiscardDraftClick)

        val state = viewModel.uiState.value
        assertEquals("", state.companyName)
        assertEquals("", state.jobTitle)
        assertEquals("Draft discarded", state.draftSavedMessage)
    }

    @Test
    fun manualSaveDraft_updatesDraftMessage() = runTest(testDispatcher) {
        val viewModel = AddEditJobViewModel(
            jobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository),
            deleteJobApplicationUseCase = DeleteJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(AddEditJobUiEvent.OnCompanyNameChange("Apple"))
        viewModel.onEvent(AddEditJobUiEvent.OnSaveDraftClick)

        val state = viewModel.uiState.value
        assertEquals("Apple", state.companyName)
    }

    @Test
    fun onApplyParsedJob_populatesFieldsCorrectly() = runTest(testDispatcher) {
        val viewModel = AddEditJobViewModel(
            jobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository),
            deleteJobApplicationUseCase = DeleteJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(
            AddEditJobUiEvent.OnApplyParsedJob(
                companyName = "Frontier Semiconductor Bangladesh Ltd",
                jobTitle = "Software Engineer",
                salary = "50,000 - 60,000 BDT",
                notes = "B.Sc. in CSE, Proficient in C++ and C#"
            )
        )

        val state = viewModel.uiState.value
        assertEquals("Frontier Semiconductor Bangladesh Ltd", state.companyName)
        assertEquals("Software Engineer", state.jobTitle)
        assertEquals("50,000 - 60,000 BDT", state.salary)
        assertEquals("B.Sc. in CSE, Proficient in C++ and C#", state.notes)
        assertEquals("Auto-filled from job description!", state.draftSavedMessage)
    }
}
