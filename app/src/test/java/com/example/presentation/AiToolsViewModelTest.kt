package com.example.presentation

import com.example.data.remote.GeminiService
import com.example.data.remote.ParsedJobData
import com.example.domain.model.JobApplication
import com.example.domain.usecase.GenerateCoverLetterUseCase
import com.example.domain.usecase.GenerateFollowUpEmailUseCase
import com.example.domain.usecase.GetJobByIdUseCase
import com.example.domain.usecase.ParseJobDescriptionUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase
import com.example.presentation.ai.AiToolType
import com.example.presentation.ai.AiToolsUiEvent
import com.example.presentation.ai.AiToolsViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiToolsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeJobRepository
    private lateinit var fakeGeminiService: FakeGeminiService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeJobRepository(
            listOf(
                JobApplication(
                    id = 1L,
                    companyName = "Stripe",
                    jobTitle = "Staff Engineer",
                    dateApplied = "2026-09-01",
                    notes = "Kotlin and Distributed Systems"
                )
            )
        )
        fakeGeminiService = FakeGeminiService()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onResetState_clearsInputAndParsedOutput() = runTest(testDispatcher) {
        val viewModel = AiToolsViewModel(
            currentJobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            parseJobDescriptionUseCase = ParseJobDescriptionUseCase(fakeGeminiService),
            generateCoverLetterUseCase = GenerateCoverLetterUseCase(fakeGeminiService),
            generateFollowUpEmailUseCase = GenerateFollowUpEmailUseCase(fakeGeminiService),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(AiToolsUiEvent.OnRawJobInputChange("Software Engineer at Google"))
        viewModel.onEvent(AiToolsUiEvent.OnGenerateClick)
        testScheduler.advanceUntilIdle()

        val stateAfterGen = viewModel.uiState.value
        assertNotNull(stateAfterGen.parsedJobResult)
        assertEquals("Google", stateAfterGen.parsedJobResult?.companyName)

        // Reset state after adding / applying job
        viewModel.onEvent(AiToolsUiEvent.OnResetState)
        testScheduler.advanceUntilIdle()

        val stateAfterReset = viewModel.uiState.value
        assertEquals("", stateAfterReset.rawJobInput)
        assertEquals("", stateAfterReset.generatedOutput)
        assertNull(stateAfterReset.parsedJobResult)
        assertNull(stateAfterReset.errorMessage)
        assertNull(stateAfterReset.successMessage)
    }

    @Test
    fun onClearOutput_clearsOutputOnly() = runTest(testDispatcher) {
        val viewModel = AiToolsViewModel(
            currentJobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            parseJobDescriptionUseCase = ParseJobDescriptionUseCase(fakeGeminiService),
            generateCoverLetterUseCase = GenerateCoverLetterUseCase(fakeGeminiService),
            generateFollowUpEmailUseCase = GenerateFollowUpEmailUseCase(fakeGeminiService),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(AiToolsUiEvent.OnRawJobInputChange("Android Developer at Netflix"))
        viewModel.onEvent(AiToolsUiEvent.OnGenerateClick)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(AiToolsUiEvent.OnClearOutput)

        val state = viewModel.uiState.value
        assertEquals("Android Developer at Netflix", state.rawJobInput)
        assertEquals("", state.generatedOutput)
        assertNull(state.parsedJobResult)
    }

    @Test
    fun onSelectTool_switchesToolProperly() = runTest(testDispatcher) {
        val viewModel = AiToolsViewModel(
            currentJobId = 0L,
            getJobByIdUseCase = GetJobByIdUseCase(fakeRepository),
            parseJobDescriptionUseCase = ParseJobDescriptionUseCase(fakeGeminiService),
            generateCoverLetterUseCase = GenerateCoverLetterUseCase(fakeGeminiService),
            generateFollowUpEmailUseCase = GenerateFollowUpEmailUseCase(fakeGeminiService),
            saveJobApplicationUseCase = SaveJobApplicationUseCase(fakeRepository)
        )

        viewModel.onEvent(AiToolsUiEvent.OnSelectTool(AiToolType.CoverLetter))
        assertEquals(AiToolType.CoverLetter, viewModel.uiState.value.currentTool)
    }
}

class FakeGeminiService : GeminiService() {
    override suspend fun parseJobPosting(rawText: String): Result<ParsedJobData> {
        return Result.success(
            ParsedJobData(
                companyName = if (rawText.contains("Google")) "Google" else "Netflix",
                jobTitle = "Software Engineer",
                salary = "$180,000",
                keyPoints = "Kotlin, Compose"
            )
        )
    }

    override suspend fun generateCoverLetter(
        companyName: String,
        jobTitle: String,
        notesOrDescription: String,
        userCandidateHighlights: String
    ): Result<String> {
        return Result.success("Dear Hiring Team at $companyName...")
    }

    override suspend fun generateFollowUpEmail(
        application: JobApplication,
        emailType: String
    ): Result<String> {
        return Result.success("Subject: Follow up on ${application.jobTitle} at ${application.companyName}")
    }
}
