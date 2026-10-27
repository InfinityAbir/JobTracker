package com.example.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.ApiKeyStore
import com.example.data.remote.ParsedJobData
import com.example.data.remote.SharedLimitException
import com.example.domain.model.JobApplication
import com.example.localization.AppStrings
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

/** The output is shown as plain text, so drop the markdown symbols Gemini likes to add. */
internal fun stripMarkdown(text: String): String = text
    .replace(Regex("(?m)^[ \\t]{0,3}#{1,6}[ \\t]*"), "")
    .replace(Regex("(?m)^[ \\t]*([-*_])\\1{2,}[ \\t]*$"), "")
    .replace("**", "")
    .replace(Regex("(?m)^[ \\t]*\\*[ \\t]+"), "• ")
    .replace(Regex("\\n{3,}"), "\n\n")
    .trim()

class AiToolsViewModel(
    private val currentJobId: Long = 0L,
    private val getJobByIdUseCase: GetJobByIdUseCase,
    private val parseJobDescriptionUseCase: ParseJobDescriptionUseCase,
    private val generateCoverLetterUseCase: GenerateCoverLetterUseCase,
    private val generateFollowUpEmailUseCase: GenerateFollowUpEmailUseCase,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase,
    private val apiKeyStore: ApiKeyStore? = null,
    proxyAvailable: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiToolsUiState(hasApiKey = apiKeyStore?.hasKey() ?: true, proxyAvailable = proxyAvailable)
    )
    val uiState: StateFlow<AiToolsUiState> = _uiState.asStateFlow()

    private var activeJob: JobApplication? = null

    private companion object {
        // Without a saved application the user only types free text, so tell the model to take the names from it.
        const val COMPANY_FROM_DETAILS = "the company named in the details below"
        const val ROLE_FROM_DETAILS = "the role named in the details below"
    }

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
            is AiToolsUiEvent.OnApiKeyInputChange -> {
                _uiState.update { it.copy(apiKeyInput = event.text) }
            }
            AiToolsUiEvent.OnShowKeyEntry -> {
                _uiState.update { it.copy(showKeyEntry = true) }
            }
            AiToolsUiEvent.OnSaveApiKey -> {
                val key = _uiState.value.apiKeyInput.trim()
                if (key.isNotBlank()) {
                    apiKeyStore?.set(key)
                    _uiState.update {
                        it.copy(hasApiKey = true, apiKeyInput = "", showKeyEntry = false, errorMessage = null)
                    }
                }
            }
            AiToolsUiEvent.OnRemoveApiKey -> {
                apiKeyStore?.clear()
                _uiState.update {
                    it.copy(hasApiKey = false, apiKeyInput = "", showKeyEntry = false, errorMessage = null)
                }
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
                successMessage = null,
                hasApiKey = it.hasApiKey,
                proxyAvailable = it.proxyAvailable,
                showKeyEntry = it.showKeyEntry
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
                                    successMessage = AppStrings.current().aiParsedOk
                                )
                            }
                        },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = err.message ?: AppStrings.current().aiParseFailed, showKeyEntry = it.showKeyEntry || err is SharedLimitException)
                            }
                        }
                    )
                }

                AiToolType.CoverLetter -> {
                    val company = activeJob?.companyName ?: COMPANY_FROM_DETAILS
                    val role = activeJob?.jobTitle ?: ROLE_FROM_DETAILS
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
                                it.copy(isLoading = false, generatedOutput = stripMarkdown(letter))
                            }
                        },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = err.message ?: AppStrings.current().aiCoverFailed, showKeyEntry = it.showKeyEntry || err is SharedLimitException)
                            }
                        }
                    )
                }

                AiToolType.FollowUpEmail -> {
                    val job = activeJob ?: JobApplication(
                        companyName = COMPANY_FROM_DETAILS,
                        jobTitle = ROLE_FROM_DETAILS,
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
                                it.copy(isLoading = false, generatedOutput = stripMarkdown(email))
                            }
                        },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = err.message ?: AppStrings.current().aiEmailFailed, showKeyEntry = it.showKeyEntry || err is SharedLimitException)
                            }
                        }
                    )
                }
            }
        }
    }
}
