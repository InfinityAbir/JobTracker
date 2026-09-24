package com.example.presentation.ai

import com.example.data.remote.ParsedJobData

sealed interface AiToolType {
    data object AutoParseJob : AiToolType
    data object CoverLetter : AiToolType
    data object FollowUpEmail : AiToolType
}

data class AiToolsUiState(
    val currentTool: AiToolType = AiToolType.AutoParseJob,
    val rawJobInput: String = "",
    val candidateProfileInput: String = "",
    val selectedEmailType: String = "Post-Interview Thank You",
    val generatedOutput: String = "",
    val parsedJobResult: ParsedJobData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface AiToolsUiEvent {
    data class OnSelectTool(val tool: AiToolType) : AiToolsUiEvent
    data class OnRawJobInputChange(val text: String) : AiToolsUiEvent
    data class OnCandidateProfileChange(val text: String) : AiToolsUiEvent
    data class OnEmailTypeChange(val emailType: String) : AiToolsUiEvent
    data object OnGenerateClick : AiToolsUiEvent
    data object OnClearOutput : AiToolsUiEvent
    data object OnResetState : AiToolsUiEvent
    data object OnDismissMessage : AiToolsUiEvent
}
