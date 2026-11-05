package com.example.presentation.ai

import com.example.data.remote.MatchAnalysisResult
import com.example.data.remote.ParsedJobData

sealed interface AiToolType {
    data object AutoParseJob : AiToolType
    data object CoverLetter : AiToolType
    data object FollowUpEmail : AiToolType
    data object MatchAnalysis : AiToolType
}

/** Dropdown value meaning "the user writes their own scenario" (see [AiToolsUiState.customEmailScenario]). */
const val CUSTOM_EMAIL_TYPE = "__custom__"

/** The AI proxy truncates the scenario at 100 characters, so the field is capped to match. */
const val MAX_CUSTOM_SCENARIO_LENGTH = 100

data class AiToolsUiState(
    val currentTool: AiToolType = AiToolType.AutoParseJob,
    val rawJobInput: String = "",
    val candidateProfileInput: String = "",
    val selectedEmailType: String = "Post-Interview Thank You",
    val customEmailScenario: String = "",
    val generatedOutput: String = "",
    val parsedJobResult: ParsedJobData? = null,
    val matchResult: MatchAnalysisResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasApiKey: Boolean = true,
    val apiKeyInput: String = "",
    val proxyAvailable: Boolean = false,
    val showKeyEntry: Boolean = false
) {
    val showKeyCard: Boolean get() = !hasApiKey && (!proxyAvailable || showKeyEntry)
}

sealed interface AiToolsUiEvent {
    data class OnSelectTool(val tool: AiToolType) : AiToolsUiEvent
    data class OnRawJobInputChange(val text: String) : AiToolsUiEvent
    data class OnCandidateProfileChange(val text: String) : AiToolsUiEvent
    data class OnEmailTypeChange(val emailType: String) : AiToolsUiEvent
    data class OnCustomScenarioChange(val text: String) : AiToolsUiEvent
    data class OnApiKeyInputChange(val text: String) : AiToolsUiEvent
    data object OnShowKeyEntry : AiToolsUiEvent
    data object OnSaveApiKey : AiToolsUiEvent
    data object OnRemoveApiKey : AiToolsUiEvent
    data object OnGenerateClick : AiToolsUiEvent
    data object OnClearOutput : AiToolsUiEvent
    data object OnResetState : AiToolsUiEvent
    data object OnDismissMessage : AiToolsUiEvent
}
