package com.example.presentation.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ParsedJobData
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroButton
import com.example.presentation.components.RetroButtonStyle
import com.example.presentation.components.RetroFieldShape
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroTag
import com.example.presentation.components.hardShadow
import com.example.presentation.components.retroTextFieldColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiToolsBottomSheet(
    state: AiToolsUiState,
    onEvent: (AiToolsUiEvent) -> Unit,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onApplyParsedData: ((ParsedJobData) -> Unit)? = null,
    isFromExistingForm: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.isImeVisible || WindowInsets.ime.getBottom(density) > 0
    var hasFocus by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("ai_tools_bottom_sheet")
    ) {
        // BackHandler inside bottom sheet dialog:
        // If keyboard/focus is active, first back press hides keyboard and clears focus.
        // Once keyboard/focus is gone, second back press dismisses the bottom sheet.
        BackHandler {
            if (isImeVisible || hasFocus) {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
                hasFocus = false
            } else {
                onDismiss()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .hardShadow(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = strings.aiSheetTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.aiPoweredBy,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (state.rawJobInput.isNotBlank() || state.generatedOutput.isNotBlank() || state.parsedJobResult != null) {
                        RetroButton(
                            text = strings.clearAll,
                            onClick = { onEvent(AiToolsUiEvent.OnResetState) },
                            style = RetroButtonStyle.Danger,
                            compact = true,
                            modifier = Modifier.testTag("ai_clear_all_button")
                        )
                    }

                    RetroIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close AI Tools",
                        onClick = onDismiss,
                        size = 38.dp,
                        shadowOffset = 2.dp,
                        modifier = Modifier.testTag("ai_close_sheet_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Tools Tab Selector
            val tabs = listOf(
                Triple(AiToolType.AutoParseJob, strings.tabJobParser, Icons.AutoMirrored.Filled.FactCheck),
                Triple(AiToolType.CoverLetter, strings.tabCoverLetter, Icons.Default.Description),
                Triple(AiToolType.FollowUpEmail, strings.tabFollowUp, Icons.Default.Email)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                tabs.forEachIndexed { index, (tool, label, icon) ->
                    val isSelected = state.currentTool == tool
                    val background = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val content = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(background)
                            .clickable(onClick = { onEvent(AiToolsUiEvent.OnSelectTool(tool)) })
                            .testTag("ai_tool_tab_${tool::class.simpleName}")
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(imageVector = icon, contentDescription = label, tint = content, modifier = Modifier.size(16.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 0.6.sp
                                ),
                                color = content,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tool Description / Explainer
            Text(
                text = when (state.currentTool) {
                    AiToolType.AutoParseJob -> strings.parserExplainer
                    AiToolType.CoverLetter -> strings.coverLetterExplainer
                    AiToolType.FollowUpEmail -> strings.followUpExplainer
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Input Fields
            when (state.currentTool) {
                AiToolType.AutoParseJob -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.jobPostingLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.rawJobInput.isNotBlank()) {
                            RetroButton(
                                onClick = { onEvent(AiToolsUiEvent.OnRawJobInputChange("")) },
                                text = strings.clearInput,
                                style = RetroButtonStyle.Secondary,
                                compact = true,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.rawJobInput,
                        onValueChange = { onEvent(AiToolsUiEvent.OnRawJobInputChange(it)) },
                        placeholder = { Text(strings.jobPostingPlaceholder) },
                        minLines = 4,
                        maxLines = 8,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) hasFocus = true }
                            .testTag("raw_job_input"),
                        colors = retroTextFieldColors()
                    )
                }

                AiToolType.CoverLetter -> {
                    Text(
                        text = strings.targetRoleLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.rawJobInput,
                        onValueChange = { onEvent(AiToolsUiEvent.OnRawJobInputChange(it)) },
                        placeholder = { Text(strings.targetRolePlaceholder) },
                        singleLine = true,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) hasFocus = true }
                            .testTag("cover_letter_target_input"),
                        colors = retroTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = strings.candidateProfileLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.candidateProfileInput,
                        onValueChange = { onEvent(AiToolsUiEvent.OnCandidateProfileChange(it)) },
                        placeholder = { Text(strings.candidateProfilePlaceholder) },
                        minLines = 3,
                        maxLines = 6,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) hasFocus = true }
                            .testTag("candidate_profile_input"),
                        colors = retroTextFieldColors()
                    )
                }

                AiToolType.FollowUpEmail -> {
                    Text(
                        text = strings.emailScenarioLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val emailTypes = listOf(
                        "Post-Interview Thank You",
                        "Application Status Check-In",
                        "Acceptance & Gratitude",
                        "Declining Offer Politely"
                    )
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.selectedEmailType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RetroFieldShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("email_scenario_dropdown"),
                            colors = retroTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            emailTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        onEvent(AiToolsUiEvent.OnEmailTypeChange(type))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = strings.emailNotesLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.rawJobInput,
                        onValueChange = { onEvent(AiToolsUiEvent.OnRawJobInputChange(it)) },
                        placeholder = { Text(strings.emailNotesPlaceholder) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) hasFocus = true }
                            .testTag("email_notes_input"),
                        colors = retroTextFieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            RetroButton(
                text = when (state.currentTool) {
                    AiToolType.AutoParseJob -> strings.btnExtractJob
                    AiToolType.CoverLetter -> strings.btnGenerateCoverLetter
                    AiToolType.FollowUpEmail -> strings.btnDraftEmail
                },
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onEvent(AiToolsUiEvent.OnGenerateClick)
                },
                icon = Icons.Default.AutoAwesome,
                style = RetroButtonStyle.Primary,
                enabled = !state.isLoading,
                loading = state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("ai_generate_button")
            )

            // Error Message
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                RetroPanel(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    borderColor = MaterialTheme.colorScheme.error,
                    shadowColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(13.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                RetroButton(
                                    text = strings.retryButton,
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onEvent(AiToolsUiEvent.OnGenerateClick)
                                    },
                                    icon = Icons.Default.Refresh,
                                    style = RetroButtonStyle.Danger,
                                    compact = true
                                )
                            }
                        }
                    }
                }
            }

            // Output Display Area
            if (state.generatedOutput.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.generatedOutputHeader,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        RetroIconButton(
                            icon = Icons.Default.ContentCopy,
                            contentDescription = "Copy Output",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AI Job Assistant", state.generatedOutput)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            size = 38.dp,
                            shadowOffset = 2.dp,
                            modifier = Modifier.testTag("copy_output_button")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                RetroPanel(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = MaterialTheme.colorScheme.outline,
                    shadowOffset = 2.dp,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(13.dp)
                        ) {
                            Text(
                                text = state.generatedOutput,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Dedicated Professional Auto-Fill Action Card for Parsed Job Descriptions
                if (state.currentTool == AiToolType.AutoParseJob && state.parsedJobResult != null && onApplyParsedData != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    RetroPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_parsed_job_card"),
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        borderColor = MaterialTheme.colorScheme.primary,
                        shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(13.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .hardShadow(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = strings.readyToAutoFillHeader,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isFromExistingForm) {
                                                strings.autoFillDescCurrent
                                            } else {
                                                strings.autoFillDescNew
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Quick preview chips of extracted data
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (state.parsedJobResult.companyName.isNotBlank()) {
                                        RetroTag(
                                            text = state.parsedJobResult.companyName,
                                            icon = Icons.Default.Business,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (state.parsedJobResult.jobTitle.isNotBlank()) {
                                        RetroTag(
                                            text = state.parsedJobResult.jobTitle,
                                            icon = Icons.Default.Work,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (state.parsedJobResult.salary.isNotBlank()) {
                                        RetroTag(
                                            text = state.parsedJobResult.salary,
                                            icon = Icons.Default.AttachMoney,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            borderColor = MaterialTheme.colorScheme.secondary,
                                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                        )
                                    }
                                }

                                RetroButton(
                                    text = if (isFromExistingForm) strings.btnFillCurrentForm else strings.btnCreateWithExtracted,
                                    onClick = {
                                        val parsed = state.parsedJobResult
                                        onEvent(AiToolsUiEvent.OnResetState)
                                        onApplyParsedData?.invoke(parsed)
                                        onDismiss()
                                    },
                                    icon = if (isFromExistingForm) Icons.Default.Done else Icons.Default.PostAdd,
                                    style = RetroButtonStyle.Primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_parsed_job_button")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
