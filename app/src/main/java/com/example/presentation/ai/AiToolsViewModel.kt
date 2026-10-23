package com.example.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.ParsedJobData
import com.example.domain.model.JobApplication
import com.example.domain.usecase.GenerateCoverLetterUseCase
import com.example.domain.usecase.GenerateFollowUpEmailUseCase
import com.example.domain.usecase.GetJobByIdUseCase
import com.example.domain.usecase.ParseJobDescriptionUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiToolsViewModel(
    private val currentJobId: Long = 0L,
    private val getJobByIdUseCase: GetJobByIdUseCase,
    private val parseJobDescriptionUseCase: ParseJobDescriptionUseCase,
    private val generateCoverLetterUseCase: GenerateCoverLetterUseCase,
    private val generateFollowUpEmailUseCase: GenerateFollowUpEmailUseCase,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiToolsUiState())
    val uiState: StateFlow<AiToolsUiState> = _uiState.asStateFlow()

    private var activeJob: JobApplication? = null

    init {
        if (currentJobId > 0) {
            viewModelScope.launch {
                val job = getJobByIdUseCase.getDirect(currentJobId)
                if (job != null) {
                    activeJob = job
                    _uiState.update {
                        it.copy(
                            currentTool = AiToolType.CoverLetter,
                            rawJobInput = "${job.jobTitle} at ${job.companyName}\n${job.notes}"
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AiToolsUiEvent) {
        when (event) {
            is AiToolsUiEvent.OnSelectTool -> {
                _uiState.update {
                    it.copy(
                        currentTool = event.tool,
                        generatedOutput = "",
                        parsedJobResult = null,
                        errorMessage = null
                    )
                }
            }
            is AiToolsUiEvent.OnRawJobInputChange -> {
                _uiState.update { it.copy(rawJobInput = event.text) }
            }
            is AiToolsUiEvent.OnCandidateProfileChange -> {
                _uiState.update { it.copy(candidateProfileInput = event.text) }
            }
            is AiToolsUiEvent.OnEmailTypeChange -> {
                _uiState.update { it.copy(selectedEmailType = event.emailType) }
            }
            AiToolsUiEvent.OnGenerateClick -> {
                executeGeneration()
            }
            AiToolsUiEvent.OnClearOutput -> {
                _uiState.update { it.copy(generatedOutput = "", parsedJobResult = null, errorMessage = null, successMessage = null) }
            }
            AiToolsUiEvent.OnResetState -> {
                resetState()
            }
            AiToolsUiEvent.OnDismissMessage -> {
                _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    fun resetState() {
        _uiState.update {
            AiToolsUiState(
                currentTool = it.currentTool,
                rawJobInput = if (activeJob != null && it.currentTool == AiToolType.CoverLetter) {
                    "${activeJob?.jobTitle} at ${activeJob?.companyName}\n${activeJob?.notes}"
                } else "",
                candidateProfileInput = "",
                selectedEmailType = "Post-Interview Thank You",
                generatedOutput = "",
                parsedJobResult = null,
                isLoading = false,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun executeGeneration() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (state.currentTool) {
                AiToolType.AutoParseJob -> {
                    val result = parseJobDescriptionUseCase(state.rawJobInput)
                    result.fold(
                        onSuccess = { parsed: ParsedJobData ->
                            val formatted = buildString {
                                appendLine("📋 Extracted Job Details:")
                                appendLine("• Company: ${parsed.companyName.ifBlank { "N/A" }}")
                                appendLine("• Role: ${parsed.jobTitle.ifBlank { "N/A" }}")
                                if (parsed.salary.isNotBlank()) appendLine("• Salary: ${parsed.salary}")
                                if (parsed.keyPoints.isNotBlank()) {
                                    appendLine("\nKey Highlights & Requirements:\n${parsed.keyPoints}")
                                }
                            }
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generatedOutput = formatted,
                                    parsedJobResult = parsed,
                                    successMessage = "Job parsed successfully!"
                                )
                            }
                        },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = err.message ?: "Failed to parse job posting")
                            }
                        }
                    )
                }

                AiToolType.CoverLetter -> {
                    val company = activeJob?.companyName ?: "Target Company"
                    val role = activeJob?.jobTitle ?: "Desired Role"
                    val notes = state.rawJobInput.ifBlank { activeJob?.notes ?: "" }

                    val result = generateCoverLetterUseCase(
                        companyName = company,
                        jobTitle = role,
                        notes = notes,
                        candidateProfile = state.candidateProfileInput
                    )
                    result.fold(
                        onSuccess = { letter ->
                            _uiState.update {
                                it.copy(isLoading = false, generatedOutput = letter)
                            }
                        },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = err.message ?: "Failed to generate cover letter")
                            }
                        }
                    )
                }

                AiToolType.FollowUpEmail -> {
                    val job = activeJob ?: JobApplication(
                        companyName = "Target Company",
                        jobTitle = "Role",
                        dateApplied = "",
                        notes = state.rawJobInput
                    )

                    val result = generateFollowUpEmailUseCase(
                        application = job,
                        emailType = state.selectedEmailType
                    )
                    result.fold(
                        onSuccess = { email ->
                            _uiState.update {
                                it.copy(isLoading = false, generatedOutput = email)
                            }
                        },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = err.message ?: "Failed to generate email draft")
                            }
                        }
                    )
                }
            }
        }
    }
}
