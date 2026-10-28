package com.example.presentation

import com.example.data.remote.GeminiService
import com.example.data.remote.MatchAnalysisResult
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.domain.usecase.AnalyzeResumeMatchUseCase
import com.example.domain.usecase.DeleteJobApplicationUseCase
import com.example.domain.usecase.GenerateCoverLetterUseCase
import com.example.domain.usecase.GenerateFollowUpEmailUseCase
import com.example.domain.usecase.GetJobByIdUseCase
import com.example.domain.usecase.ParseJobDescriptionUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase
import com.example.presentation.ai.AiToolType
import com.example.presentation.ai.AiToolsUiEvent
import com.example.presentation.ai.AiToolsViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewFeaturesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun editViewModel(repo: FakeJobRepository, id: Long) = AddEditJobViewModel(
        jobId = id,
        getJobByIdUseCase = GetJobByIdUseCase(repo),
        saveJobApplicationUseCase = SaveJobApplicationUseCase(repo),
        deleteJobApplicationUseCase = DeleteJobApplicationUseCase(repo)
    )

    private fun aiViewModel(repo: FakeJobRepository, gemini: GeminiService, jobId: Long = 0L, initialTool: AiToolType? = null) =
        AiToolsViewModel(
            currentJobId = jobId,
            getJobByIdUseCase = GetJobByIdUseCase(repo),
            parseJobDescriptionUseCase = ParseJobDescriptionUseCase(gemini),
            generateCoverLetterUseCase = GenerateCoverLetterUseCase(gemini),
            generateFollowUpEmailUseCase = GenerateFollowUpEmailUseCase(gemini),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(repo),
            analyzeResumeMatchUseCase = AnalyzeResumeMatchUseCase(gemini),
            initialTool = initialTool
        )

    // ---- AddEditJobViewModel: new fields and system-managed timestamps ----

    @Test
    fun save_movingAppliedToInterviewing_recordsTimestampAndKeepsSystemFields() = runTest(testDispatcher) {
        val repo = FakeJobRepository(
            listOf(
                JobApplication(
                    id = 1, companyName = "Stripe", jobTitle = "Dev", dateApplied = "2026-09-01",
                    status = JobStatus.APPLIED, createdAt = 111L, lastFollowUpAt = 222L
                )
            )
        )
        val vm = editViewModel(repo, 1)
        testScheduler.advanceUntilIdle()

        vm.onEvent(AddEditJobUiEvent.OnStatusChange(JobStatus.INTERVIEWING))
        vm.onEvent(AddEditJobUiEvent.OnSaveClick)
        testScheduler.advanceUntilIdle()

        val saved = repo.getApplicationByIdDirect(1)!!
        assertEquals(JobStatus.INTERVIEWING, saved.status)
        assertTrue("interviewingAt should be stamped", saved.interviewingAt > 0)
        assertEquals(111L, saved.createdAt)
        assertEquals(222L, saved.lastFollowUpAt)
    }

    @Test
    fun save_editingLegacyInterviewingJob_doesNotInventATimestamp() = runTest(testDispatcher) {
        val repo = FakeJobRepository(
            listOf(
                JobApplication(
                    id = 1, companyName = "Stripe", jobTitle = "Dev", dateApplied = "2026-01-01",
                    status = JobStatus.INTERVIEWING
                )
            )
        )
        val vm = editViewModel(repo, 1)
        testScheduler.advanceUntilIdle()

        vm.onEvent(AddEditJobUiEvent.OnNotesChange("updated notes"))
        vm.onEvent(AddEditJobUiEvent.OnSaveClick)
        testScheduler.advanceUntilIdle()

        assertEquals(0L, repo.getApplicationByIdDirect(1)!!.interviewingAt)
    }

    @Test
    fun save_keepsFollowUpTimestampWrittenWhileTheFormWasOpen() = runTest(testDispatcher) {
        val repo = FakeJobRepository(
            listOf(JobApplication(id = 1, companyName = "Stripe", jobTitle = "Dev", dateApplied = "2026-01-01"))
        )
        val vm = editViewModel(repo, 1)
        testScheduler.advanceUntilIdle()

        // The AI sheet drafts a follow-up while the edit form is still open.
        repo.updateApplication(repo.getApplicationByIdDirect(1)!!.copy(lastFollowUpAt = 999L))

        vm.onEvent(AddEditJobUiEvent.OnNotesChange("edited"))
        vm.onEvent(AddEditJobUiEvent.OnSaveClick)
        testScheduler.advanceUntilIdle()

        assertEquals(999L, repo.getApplicationByIdDirect(1)!!.lastFollowUpAt)
    }

    @Test
    fun save_persistsOfferDetailsAndPrepNotes_andBenefitsToggle() = runTest(testDispatcher) {
        val repo = FakeJobRepository()
        val vm = editViewModel(repo, 0)

        vm.onEvent(AddEditJobUiEvent.OnCompanyNameChange("Acme"))
        vm.onEvent(AddEditJobUiEvent.OnJobTitleChange("Engineer"))
        vm.onEvent(AddEditJobUiEvent.OnStatusChange(JobStatus.OFFER))
        vm.onEvent(AddEditJobUiEvent.OnWorkModelChange("Hybrid"))
        vm.onEvent(AddEditJobUiEvent.OnOfferTotalCompChange("190k"))
        vm.onEvent(AddEditJobUiEvent.OnOfferBenefitToggle("Health insurance"))
        vm.onEvent(AddEditJobUiEvent.OnOfferBenefitToggle("Annual bonus"))
        vm.onEvent(AddEditJobUiEvent.OnOfferBenefitToggle("Health insurance")) // toggled off again
        vm.onEvent(AddEditJobUiEvent.OnOfferProsChange("Great team"))
        vm.onEvent(AddEditJobUiEvent.OnOfferConsChange("Long commute"))
        vm.onEvent(AddEditJobUiEvent.OnPrepQuestionsChange("What is the roadmap?"))
        vm.onEvent(AddEditJobUiEvent.OnPrepStarChange("S: ..."))
        vm.onEvent(AddEditJobUiEvent.OnPrepNegotiationChange("Ask for 200k"))
        vm.onEvent(AddEditJobUiEvent.OnSaveClick)
        testScheduler.advanceUntilIdle()

        val saved = repo.getAllDirect().single()
        assertEquals("Hybrid", saved.workModel)
        assertEquals("190k", saved.offerTotalComp)
        assertEquals(setOf("Annual bonus"), saved.benefitKeys)
        assertEquals("Great team", saved.offerPros)
        assertEquals("Long commute", saved.offerCons)
        assertEquals("What is the roadmap?", saved.prepQuestions)
        assertEquals("S: ...", saved.prepStar)
        assertEquals("Ask for 200k", saved.prepNegotiation)
        // Created directly as an offer: timing is unknown, so it must not skew the average
        assertEquals(0L, saved.interviewingAt)
    }

    // ---- AiToolsViewModel: match analysis and stale follow-up ----

    @Test
    fun matchAnalysis_success_exposesStructuredResult() = runTest(testDispatcher) {
        val gemini = object : GeminiService() {
            override suspend fun analyzeMatch(jobDescriptionInput: String, resumeSummaryInput: String) =
                Result.success(MatchAnalysisResult(85, listOf("Kotlin", "Compose", "Room"), listOf("GraphQL"), "Mention testing"))
        }
        val vm = aiViewModel(FakeJobRepository(), gemini)

        vm.onEvent(AiToolsUiEvent.OnSelectTool(AiToolType.MatchAnalysis))
        vm.onEvent(AiToolsUiEvent.OnRawJobInputChange("Needs Kotlin, Compose, GraphQL"))
        vm.onEvent(AiToolsUiEvent.OnCandidateProfileChange("5 years Kotlin, Compose, Room"))
        vm.onEvent(AiToolsUiEvent.OnGenerateClick)
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.errorMessage)
        assertEquals(85, state.matchResult?.matchPercent)
        assertEquals(listOf("GraphQL"), state.matchResult?.missingKeywords)
        assertTrue(state.generatedOutput.contains("85%"))
        assertTrue(state.generatedOutput.contains("GraphQL"))

        vm.onEvent(AiToolsUiEvent.OnClearOutput)
        assertNull(vm.uiState.value.matchResult)
    }

    @Test
    fun matchAnalysis_needsBothInputs() = runTest(testDispatcher) {
        var called = false
        val gemini = object : GeminiService() {
            override suspend fun analyzeMatch(jobDescriptionInput: String, resumeSummaryInput: String): Result<MatchAnalysisResult> {
                called = true
                return Result.failure(IllegalStateException("should not be called"))
            }
        }
        val vm = aiViewModel(FakeJobRepository(), gemini)

        vm.onEvent(AiToolsUiEvent.OnSelectTool(AiToolType.MatchAnalysis))
        vm.onEvent(AiToolsUiEvent.OnRawJobInputChange("Only a job description"))
        vm.onEvent(AiToolsUiEvent.OnGenerateClick)
        testScheduler.advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.matchResult)
        assertTrue(!called)
        assertTrue(!vm.uiState.value.isLoading)
    }

    @Test
    fun followUpFromStaleCard_opensFollowUpToolAndRestartsStaleClock() = runTest(testDispatcher) {
        val repo = FakeJobRepository(
            listOf(JobApplication(id = 7, companyName = "Stripe", jobTitle = "Dev", dateApplied = "2026-08-01"))
        )
        val vm = aiViewModel(repo, FakeGeminiService(), jobId = 7, initialTool = AiToolType.FollowUpEmail)
        testScheduler.advanceUntilIdle()

        assertEquals(AiToolType.FollowUpEmail, vm.uiState.value.currentTool)
        assertEquals("Application Status Check-In", vm.uiState.value.selectedEmailType)

        vm.onEvent(AiToolsUiEvent.OnGenerateClick)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.generatedOutput.contains("Stripe"))
        assertTrue(repo.getApplicationByIdDirect(7)!!.lastFollowUpAt > 0)
    }

    @Test
    fun openingAiSheetNormally_stillDefaultsToCoverLetter() = runTest(testDispatcher) {
        val repo = FakeJobRepository(
            listOf(JobApplication(id = 7, companyName = "Stripe", jobTitle = "Dev", dateApplied = "2026-08-01"))
        )
        val vm = aiViewModel(repo, FakeGeminiService(), jobId = 7)
        testScheduler.advanceUntilIdle()
        assertEquals(AiToolType.CoverLetter, vm.uiState.value.currentTool)
    }

    @Test
    fun unexpectedAiFailure_neverShowsRawExceptionText() = runTest(testDispatcher) {
        val gemini = object : GeminiService() {
            override suspend fun analyzeMatch(jobDescriptionInput: String, resumeSummaryInput: String): Result<MatchAnalysisResult> =
                Result.failure(RuntimeException("org.json.JSONException: Value <html> of type java.lang.String cannot be converted"))
        }
        val vm = aiViewModel(FakeJobRepository(), gemini)

        vm.onEvent(AiToolsUiEvent.OnSelectTool(AiToolType.MatchAnalysis))
        vm.onEvent(AiToolsUiEvent.OnRawJobInputChange("job"))
        vm.onEvent(AiToolsUiEvent.OnCandidateProfileChange("resume"))
        vm.onEvent(AiToolsUiEvent.OnGenerateClick)
        testScheduler.advanceUntilIdle()

        val message = vm.uiState.value.errorMessage
        assertNotNull(message)
        assertTrue(!message!!.contains("Exception") && !message.contains("java."))
    }
}
