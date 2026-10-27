package com.example.presentation.detail

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.presentation.dashboard.components.StatusChip
import com.example.util.InterviewNotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.localization.LocalAppStrings
import com.example.presentation.components.LanguageToggleButton
import com.example.presentation.components.RetroButton
import com.example.presentation.components.RetroButtonStyle
import com.example.presentation.components.RetroFieldShape
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroTopBarRule
import com.example.presentation.components.retroTextFieldColors
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditJobScreen(
    state: AddEditJobUiState,
    onEvent: (AddEditJobUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenAiTools: () -> Unit,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onLanguageChange: (AppLanguage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    val hasUnsavedContent = remember(
        state.companyName,
        state.jobTitle,
        state.salary,
        state.notes,
        state.recruiterName,
        state.recruiterEmail,
        state.recruiterPhone,
        state.referralSource,
        state.interviewDate,
        state.interviewNotes
    ) {
        state.companyName.isNotBlank() ||
                state.jobTitle.isNotBlank() ||
                state.salary.isNotBlank() ||
                state.notes.isNotBlank() ||
                state.recruiterName.isNotBlank() ||
                state.recruiterEmail.isNotBlank() ||
                state.recruiterPhone.isNotBlank() ||
                state.referralSource.isNotBlank() ||
                state.interviewDate.isNotBlank() ||
                state.interviewNotes.isNotBlank()
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // 1. First priority: Dismiss keyboard when back is pressed while keyboard is active
    BackHandler(enabled = isImeVisible) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    // 2. Second priority: If keyboard is closed and there are unsaved changes in a new job, show Unsaved Dialog
    BackHandler(enabled = !isImeVisible && hasUnsavedContent && state.id == 0L && !showUnsavedDialog) {
        showUnsavedDialog = true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var isDraftSaved by remember { mutableStateOf(false) }

    LaunchedEffect(
        state.companyName,
        state.jobTitle,
        state.salary,
        state.notes,
        state.recruiterName,
        state.recruiterEmail,
        state.recruiterPhone,
        state.referralSource,
        state.interviewDate,
        state.interviewNotes
    ) {
        if (state.draftSavedMessage == null) {
            isDraftSaved = false
        }
    }

    LaunchedEffect(state.draftSavedMessage) {
        state.draftSavedMessage?.let { msg ->
            if (msg == strings.msgDraftDiscardedForm) {
                isDraftSaved = false
            } else if (msg == strings.msgDraftSaved) {
                isDraftSaved = true
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            onEvent(AddEditJobUiEvent.OnDismissDraftMessage)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, strings.toastNotifEnabled, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, strings.toastNotifDenied, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            if (state.reminderSet && state.interviewDate.isNotBlank()) {
                val tempApp = JobApplication(
                    id = if (state.id > 0) state.id else System.currentTimeMillis() % 100000,
                    companyName = state.companyName,
                    jobTitle = state.jobTitle,
                    dateApplied = state.dateApplied,
                    status = state.status,
                    interviewDate = state.interviewDate,
                    interviewTime = state.interviewTime,
                    interviewType = state.interviewType,
                    interviewNotes = state.interviewNotes,
                    reminderSet = state.reminderSet,
                    reminderMinutesBefore = state.reminderMinutesBefore
                )
                val scheduled = InterviewNotificationHelper.scheduleInterviewAlarm(context, tempApp)
                if (scheduled) {
                    Toast.makeText(context, strings.toastReminderScheduled, Toast.LENGTH_SHORT).show()
                }
            } else if (state.id > 0) {
                InterviewNotificationHelper.cancelInterviewAlarm(context, state.id)
            }
            onNavigateBack()
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            if (state.id > 0) {
                InterviewNotificationHelper.cancelInterviewAlarm(context, state.id)
            }
            onNavigateBack()
        }
    }

    // Unsaved Changes / Draft confirmation dialog
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(4.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Drafts,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = strings.unsavedDialogTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.unsavedDialogMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                RetroButton(
                    text = strings.saveDraftAndExit,
                    onClick = {
                        showUnsavedDialog = false
                        onEvent(AddEditJobUiEvent.OnSaveDraftClick)
                        onNavigateBack()
                    },
                    style = RetroButtonStyle.Primary,
                    compact = true,
                    modifier = Modifier.testTag("save_draft_dialog_button")
                )
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RetroButton(
                        text = strings.discard,
                        onClick = {
                            showUnsavedDialog = false
                            onEvent(AddEditJobUiEvent.OnDiscardDraftClick)
                            onNavigateBack()
                        },
                        style = RetroButtonStyle.Danger,
                        compact = true,
                        modifier = Modifier.testTag("discard_draft_dialog_button")
                    )
                    RetroButton(
                        text = strings.keepEditing,
                        onClick = { showUnsavedDialog = false },
                        style = RetroButtonStyle.Secondary,
                        compact = true,
                        modifier = Modifier.testTag("cancel_unsaved_dialog_button")
                    )
                }
            },
            modifier = Modifier.testTag("unsaved_changes_dialog")
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(4.dp),
            title = {
                Text(
                    text = strings.deleteDialogTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.deleteDialogMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                RetroButton(
                    text = strings.deleteButton,
                    onClick = {
                        showDeleteDialog = false
                        onEvent(AddEditJobUiEvent.OnDeleteClick)
                    },
                    style = RetroButtonStyle.Danger,
                    compact = true,
                    modifier = Modifier.testTag("confirm_delete_button")
                )
            },
            dismissButton = {
                RetroButton(
                    text = strings.cancelButton,
                    onClick = { showDeleteDialog = false },
                    style = RetroButtonStyle.Secondary,
                    compact = true,
                    modifier = Modifier.testTag("cancel_delete_button")
                )
            },
            modifier = Modifier.testTag("delete_confirmation_dialog")
        )
    }

    Scaffold(
        modifier = modifier.testTag("add_edit_job_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.testTag("add_edit_snackbar_host")) },
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.backButtonDesc,
                        onClick = {
                            if (hasUnsavedContent && state.id == 0L) {
                                showUnsavedDialog = true
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("back_button"),
                        size = 42.dp,
                        shadowOffset = 2.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.id > 0) strings.editApplicationTitle else strings.newApplicationTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = strings.aiAssistantDesc,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LanguageToggleButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange,
                        modifier = Modifier
                            .testTag("language_toggle_button")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    RetroIconButton(
                        icon = Icons.Default.AutoAwesome,
                        contentDescription = strings.aiAssistantDesc,
                        onClick = onOpenAiTools,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        size = 42.dp,
                        shadowOffset = 2.dp,
                        modifier = Modifier.testTag("detail_ai_tools_button")
                    )

                    if (state.id > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        RetroIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = strings.deleteApplicationDesc,
                            onClick = { showDeleteDialog = true },
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            size = 42.dp,
                            shadowOffset = 2.dp,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("delete_job_icon_button")
                        )
                    }
                }
                RetroTopBarRule()
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                RetroTopBarRule()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    RetroButton(
                        text = if (state.id > 0) strings.saveChanges else strings.addApplicationButton,
                        onClick = { onEvent(AddEditJobUiEvent.OnSaveClick) },
                        icon = Icons.Default.Save,
                        style = RetroButtonStyle.Primary,
                        enabled = !state.isSaving,
                        loading = state.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_job_button")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Draft Restored Banner
            AnimatedVisibility(
                visible = state.isDraftRestored && state.id == 0L,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                RetroPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("draft_restored_banner"),
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    shadowColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(13.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = strings.draftRestoredTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                IconButton(
                                    onClick = { onEvent(AddEditJobUiEvent.OnDismissDraftBanner) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Text(
                                text = strings.draftRestoredSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                RetroButton(
                                    text = strings.discardDraft,
                                    onClick = { onEvent(AddEditJobUiEvent.OnDiscardDraftClick) },
                                    style = RetroButtonStyle.Danger,
                                    compact = true,
                                    modifier = Modifier.testTag("banner_discard_draft_button")
                                )
                            }
                        }
                    }
                }
            }

            // Company Name Field
            OutlinedTextField(
                value = state.companyName,
                onValueChange = { onEvent(AddEditJobUiEvent.OnCompanyNameChange(it)) },
                label = { Text(strings.companyNameLabel) },
                placeholder = { Text(strings.companyNamePlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = state.companyNameError != null,
                supportingText = {
                    state.companyNameError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RetroFieldShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("company_name_input"),
                colors = retroTextFieldColors()
            )

            // Job Title Field
            OutlinedTextField(
                value = state.jobTitle,
                onValueChange = { onEvent(AddEditJobUiEvent.OnJobTitleChange(it)) },
                label = { Text(strings.jobTitleLabel) },
                placeholder = { Text(strings.jobTitlePlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = state.jobTitleError != null,
                supportingText = {
                    state.jobTitleError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RetroFieldShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_title_input"),
                colors = retroTextFieldColors()
            )

            // Status Dropdown / Selector
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = strings.getStatusDisplayName(state.status),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(strings.applicationStatusLabel) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                    leadingIcon = {
                        val dotColor = when (state.status) {
                            JobStatus.APPLIED -> MaterialTheme.colorScheme.primary
                            JobStatus.INTERVIEWING -> MaterialTheme.colorScheme.tertiary
                            JobStatus.OFFER -> MaterialTheme.colorScheme.secondary
                            JobStatus.REJECTED -> MaterialTheme.colorScheme.error
                        }
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(10.dp)
                                .background(dotColor, RoundedCornerShape(2.dp))
                        )
                    },
                    shape = RetroFieldShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("status_selector"),
                    colors = retroTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded,
                    onDismissRequest = { statusDropdownExpanded = false }
                ) {
                    JobStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    StatusChip(status = status)
                                    if (status == state.status) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onEvent(AddEditJobUiEvent.OnStatusChange(status))
                                statusDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("status_option_${status.name}")
                        )
                    }
                }
            }

            // Date Applied Field & Quick Selection
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.dateApplied,
                    onValueChange = { onEvent(AddEditJobUiEvent.OnDateAppliedChange(it)) },
                    label = { Text(strings.dateAppliedLabel) },
                    placeholder = { Text("2026-09-20") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = RetroFieldShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_applied_input"),
                    colors = retroTextFieldColors()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Date Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val today = dateFormat.format(Date())
                    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.let { dateFormat.format(it.time) }

                    DateQuickChip(
                        label = strings.todayChip,
                        isSelected = state.dateApplied == today,
                        onClick = { onEvent(AddEditJobUiEvent.OnDateAppliedChange(today)) }
                    )
                    DateQuickChip(
                        label = strings.yesterdayChip,
                        isSelected = state.dateApplied == yesterday,
                        onClick = { onEvent(AddEditJobUiEvent.OnDateAppliedChange(yesterday)) }
                    )
                }
            }

            // Salary / Compensation Field
            OutlinedTextField(
                value = state.salary,
                onValueChange = { onEvent(AddEditJobUiEvent.OnSalaryChange(it)) },
                label = { Text(strings.salaryLabel) },
                placeholder = { Text(strings.salaryPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RetroFieldShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("salary_input"),
                colors = retroTextFieldColors()
            )

            // Section: Interview Schedule & Reminders (Visible only when status is INTERVIEWING)
            AnimatedVisibility(
                visible = state.status == JobStatus.INTERVIEWING,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                RetroPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("interview_schedule_card"),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    shadowOffset = 3.dp,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.interviewScheduleSection,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.5.sp,
                                letterSpacing = 1.1.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                    // Interview Date Input
                    OutlinedTextField(
                        value = state.interviewDate,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(it)) },
                        label = { Text(strings.interviewDateLabel) },
                        placeholder = { Text("2026-09-24") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_date_input"),
                        colors = retroTextFieldColors()
                    )

                    // Quick Interview Date Presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val tomorrow = Calendar.getInstance().apply { add(Calendar.DATE, 1) }.let { dateFormat.format(it.time) }
                        val in3Days = Calendar.getInstance().apply { add(Calendar.DATE, 3) }.let { dateFormat.format(it.time) }
                        val in1Week = Calendar.getInstance().apply { add(Calendar.DATE, 7) }.let { dateFormat.format(it.time) }

                        DateQuickChip(
                            label = strings.tomorrowChip,
                            isSelected = state.interviewDate == tomorrow,
                            onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(tomorrow)) }
                        )
                        DateQuickChip(
                            label = strings.in3DaysChip,
                            isSelected = state.interviewDate == in3Days,
                            onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(in3Days)) }
                        )
                        DateQuickChip(
                            label = strings.nextWeekChip,
                            isSelected = state.interviewDate == in1Week,
                            onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(in1Week)) }
                        )
                        if (state.interviewDate.isNotBlank()) {
                            DateQuickChip(
                                label = strings.clearChip,
                                isSelected = false,
                                onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange("")) }
                            )
                        }
                    }

                    // Interview Stage Chips
                    Text(
                        text = strings.interviewStageLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val stages = listOf("Recruiter Screen", "Technical Round", "System Design", "Hiring Manager", "Onsite")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        stages.forEach { stage ->
                            DateQuickChip(
                                label = strings.getInterviewStageName(stage),
                                isSelected = state.interviewType == stage,
                                onClick = {
                                    val newType = if (state.interviewType == stage) "" else stage
                                    onEvent(AddEditJobUiEvent.OnInterviewTypeChange(newType))
                                }
                            )
                        }
                    }

                    // Meeting Link or Interview Notes
                    OutlinedTextField(
                        value = state.interviewNotes,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnInterviewNotesChange(it)) },
                        label = { Text(strings.meetingLinkLabel) },
                        placeholder = { Text(strings.meetingLinkPlaceholder) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_notes_input"),
                        colors = retroTextFieldColors()
                    )

                    // Interview Time Input
                    OutlinedTextField(
                        value = state.interviewTime,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnInterviewTimeChange(it)) },
                        label = { Text(strings.interviewTimeLabel) },
                        placeholder = { Text(strings.interviewTimePlaceholder) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_time_input"),
                        colors = retroTextFieldColors()
                    )

                    // Quick Time Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        val times = listOf("09:00 AM", "10:30 AM", "01:00 PM", "02:30 PM", "04:00 PM")
                        times.forEach { t ->
                            DateQuickChip(
                                label = t,
                                isSelected = state.interviewTime == t,
                                onClick = { onEvent(AddEditJobUiEvent.OnInterviewTimeChange(t)) }
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Exact Timed Call Notification & Alarm Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (state.reminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (state.reminderSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column {
                                Text(
                                    text = strings.timedCallReminderTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.timedCallReminderSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = state.reminderSet,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                onEvent(AddEditJobUiEvent.OnReminderToggle(enabled))
                            },
                            modifier = Modifier.testTag("exact_alarm_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    if (state.reminderSet) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(3.dp)
                                )
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = strings.reminderOffsetLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                val timings = listOf(
                                    0 to if (currentLanguage == AppLanguage.BANGLA) "কলের সময়" else "At call time",
                                    15 to if (currentLanguage == AppLanguage.BANGLA) "১৫ মিনিট আগে" else "15 min before",
                                    30 to if (currentLanguage == AppLanguage.BANGLA) "৩০ মিনিট আগে" else "30 min before",
                                    60 to if (currentLanguage == AppLanguage.BANGLA) "১ ঘণ্টা আগে" else "1 hour before",
                                    1440 to if (currentLanguage == AppLanguage.BANGLA) "১ দিন আগে" else "1 day before"
                                )
                                timings.forEach { (mins, label) ->
                                    DateQuickChip(
                                        label = label,
                                        isSelected = state.reminderMinutesBefore == mins,
                                        onClick = { onEvent(AddEditJobUiEvent.OnReminderMinutesChange(mins)) }
                                    )
                                }
                            }

                            // Test Notification Button
                            RetroButton(
                                text = if (currentLanguage == AppLanguage.BANGLA) "🔔 নোটিফিকেশন সাউন্ড পরীক্ষা করুন" else "🔔 Preview / Test Notification Sound",
                                onClick = {
                                    val testJob = JobApplication(
                                        id = if (state.id > 0) state.id else 999,
                                        companyName = state.companyName.ifBlank { "Google" },
                                        jobTitle = state.jobTitle.ifBlank { "Senior Engineer" },
                                        dateApplied = state.dateApplied,
                                        interviewDate = state.interviewDate.ifBlank { "Today" },
                                        interviewTime = state.interviewTime.ifBlank { "In 15 mins" },
                                        interviewType = state.interviewType.ifBlank { "Technical Screen" },
                                        interviewNotes = state.interviewNotes.ifBlank { "Google Meet: https://meet.google.com/abc-defg-hij" }
                                    )
                                    InterviewNotificationHelper.showTestNotification(context, testJob)
                                    Toast.makeText(context, strings.toastTestNotification, Toast.LENGTH_SHORT).show()
                                },
                                style = RetroButtonStyle.Secondary,
                                icon = Icons.Default.NotificationsActive,
                                compact = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("test_notification_button")
                            )
                        }
                    }
                }
            }
            }

            // Section: Recruiter & Referral Contact
            RetroPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recruiter_contact_card"),
                backgroundColor = MaterialTheme.colorScheme.surface,
                shadowOffset = 3.dp,
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactMail,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.recruiterSectionTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.5.sp,
                                letterSpacing = 1.1.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                    // Recruiter Name
                    OutlinedTextField(
                        value = state.recruiterName,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnRecruiterNameChange(it)) },
                        label = { Text(strings.recruiterNameLabel) },
                        placeholder = { Text("e.g. Sarah Jenkins") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recruiter_name_input"),
                        colors = retroTextFieldColors()
                    )

                    // Recruiter Email
                    OutlinedTextField(
                        value = state.recruiterEmail,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnRecruiterEmailChange(it)) },
                        label = { Text(strings.recruiterEmailLabel) },
                        placeholder = { Text("e.g. sjenkins@company.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recruiter_email_input"),
                        colors = retroTextFieldColors()
                    )

                    // Phone / Profile Link
                    OutlinedTextField(
                        value = state.recruiterPhone,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnRecruiterPhoneChange(it)) },
                        label = { Text(strings.recruiterPhoneLabel) },
                        placeholder = { Text("e.g. linkedin.com/in/sarah-recruiter") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RetroFieldShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recruiter_phone_input"),
                        colors = retroTextFieldColors()
                    )

                    // Referral Source Selector
                    Text(
                        text = strings.referralSourceLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val sources = listOf(
                        "LinkedIn" to if (currentLanguage == AppLanguage.BANGLA) "লিঙ্কডইন" else "LinkedIn",
                        "Employee Referral" to if (currentLanguage == AppLanguage.BANGLA) "রেফারেল" else "Employee Referral",
                        "Company Website" to if (currentLanguage == AppLanguage.BANGLA) "কোম্পানির ওয়েবসাইট" else "Company Website",
                        "Recruiter Outreach" to if (currentLanguage == AppLanguage.BANGLA) "রিক্রুটার যোগাযোগ" else "Recruiter Outreach",
                        "Job Board" to if (currentLanguage == AppLanguage.BANGLA) "জব পোর্টাল" else "Job Board",
                        "Other" to if (currentLanguage == AppLanguage.BANGLA) "অন্যান্য" else "Other"
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        sources.forEach { (sourceKey, displayLabel) ->
                            DateQuickChip(
                                label = displayLabel,
                                isSelected = state.referralSource == sourceKey,
                                onClick = {
                                    val newSource = if (state.referralSource == sourceKey) "" else sourceKey
                                    onEvent(AddEditJobUiEvent.OnReferralSourceChange(newSource))
                                }
                            )
                        }
                    }
                }
            }
            }

            // General Notes / Details Editor
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(AddEditJobUiEvent.OnNotesChange(it)) },
                label = { Text(strings.notesSectionTitle) },
                placeholder = { Text(strings.notesPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                minLines = 3,
                maxLines = 6,
                shape = RetroFieldShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_input"),
                colors = retroTextFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DateQuickChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(2.dp)
    val background = if (isSelected) colors.primaryContainer else colors.surface
    val content = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant
    val border = if (isSelected) colors.outline else colors.outlineVariant

    Box(
        modifier = Modifier
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = 0.4.sp
            ),
            color = content,
            maxLines = 1
        )
    }
}
