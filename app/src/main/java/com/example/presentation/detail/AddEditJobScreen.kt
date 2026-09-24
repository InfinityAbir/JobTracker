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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Bookmark
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
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditJobScreen(
    state: AddEditJobUiState,
    onEvent: (AddEditJobUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenAiTools: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            if (msg.contains("discard", ignoreCase = true)) {
                isDraftSaved = false
            } else if (msg.contains("saved", ignoreCase = true)) {
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
            Toast.makeText(context, "Notifications enabled for interview alerts", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "⏰ Exact interview reminder scheduled!", Toast.LENGTH_SHORT).show()
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
            icon = {
                Icon(
                    imageVector = Icons.Default.Drafts,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(text = "Unsaved Application", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "You have unsaved information. Would you like to save this application as a draft so you can resume later, or discard it?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onEvent(AddEditJobUiEvent.OnSaveDraftClick)
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("save_draft_dialog_button")
                ) {
                    Text("Save Draft & Exit")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            onEvent(AddEditJobUiEvent.OnDiscardDraftClick)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("discard_draft_dialog_button")
                    ) {
                        Text("Discard")
                    }
                    TextButton(
                        onClick = { showUnsavedDialog = false },
                        modifier = Modifier.testTag("cancel_unsaved_dialog_button")
                    ) {
                        Text("Keep Editing")
                    }
                }
            },
            modifier = Modifier.testTag("unsaved_changes_dialog")
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Application", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this job application? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onEvent(AddEditJobUiEvent.OnDeleteClick)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("delete_confirmation_dialog")
        )
    }

    Scaffold(
        modifier = modifier.testTag("add_edit_job_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.testTag("add_edit_snackbar_host")) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.id > 0) "Edit Application" else "New Application",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedContent && state.id == 0L) {
                                showUnsavedDialog = true
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenAiTools,
                        modifier = Modifier.testTag("detail_ai_tools_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant (Job Parser, Cover Letter, Emails)",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (state.id > 0) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_job_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Application",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onEvent(AddEditJobUiEvent.OnSaveClick) },
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_job_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.id > 0) "Save Changes" else "Add Application",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("draft_restored_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Draft Restored",
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
                            text = "We restored your previously unsaved application details so you can continue where you left off.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { onEvent(AddEditJobUiEvent.OnDiscardDraftClick) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("banner_discard_draft_button")
                            ) {
                                Text("Discard Draft", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Company Name Field
            OutlinedTextField(
                value = state.companyName,
                onValueChange = { onEvent(AddEditJobUiEvent.OnCompanyNameChange(it)) },
                label = { Text("Company Name *") },
                placeholder = { Text("e.g. Google, Stripe, Airbnb") },
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("company_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Job Title Field
            OutlinedTextField(
                value = state.jobTitle,
                onValueChange = { onEvent(AddEditJobUiEvent.OnJobTitleChange(it)) },
                label = { Text("Job Title *") },
                placeholder = { Text("e.g. Senior Android Engineer") },
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Status Dropdown / Selector
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.status.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Application Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                    leadingIcon = {
                        StatusChip(status = state.status, modifier = Modifier.padding(start = 8.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("status_selector"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
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
                    label = { Text("Date Applied (YYYY-MM-DD)") },
                    placeholder = { Text("2026-09-20") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_applied_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
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
                        label = "Today",
                        isSelected = state.dateApplied == today,
                        onClick = { onEvent(AddEditJobUiEvent.OnDateAppliedChange(today)) }
                    )
                    DateQuickChip(
                        label = "Yesterday",
                        isSelected = state.dateApplied == yesterday,
                        onClick = { onEvent(AddEditJobUiEvent.OnDateAppliedChange(yesterday)) }
                    )
                }
            }

            // Salary / Compensation Field
            OutlinedTextField(
                value = state.salary,
                onValueChange = { onEvent(AddEditJobUiEvent.OnSalaryChange(it)) },
                label = { Text("Salary / Compensation (Optional)") },
                placeholder = { Text("e.g. $140,000 - $160,000 / yr") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("salary_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Section: Interview Schedule & Reminders (Visible only when status is INTERVIEWING)
            AnimatedVisibility(
                visible = state.status == JobStatus.INTERVIEWING,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("interview_schedule_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Interview Schedule & Stage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Interview Date Input
                    OutlinedTextField(
                        value = state.interviewDate,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(it)) },
                        label = { Text("Interview Date (YYYY-MM-DD)") },
                        placeholder = { Text("e.g. 2026-09-24") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_date_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
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
                            label = "Tomorrow",
                            isSelected = state.interviewDate == tomorrow,
                            onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(tomorrow)) }
                        )
                        DateQuickChip(
                            label = "+3 Days",
                            isSelected = state.interviewDate == in3Days,
                            onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(in3Days)) }
                        )
                        DateQuickChip(
                            label = "Next Week",
                            isSelected = state.interviewDate == in1Week,
                            onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange(in1Week)) }
                        )
                        if (state.interviewDate.isNotBlank()) {
                            DateQuickChip(
                                label = "Clear",
                                isSelected = false,
                                onClick = { onEvent(AddEditJobUiEvent.OnInterviewDateChange("")) }
                            )
                        }
                    }

                    // Interview Stage Chips
                    Text(
                        text = "Interview Stage",
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
                                label = stage,
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
                        label = { Text("Meeting Link / Call Notes") },
                        placeholder = { Text("Google Meet/Zoom link, dial-in, interviewer names...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Interview Time Input
                    OutlinedTextField(
                        value = state.interviewTime,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnInterviewTimeChange(it)) },
                        label = { Text("Interview / Call Time") },
                        placeholder = { Text("e.g. 10:30 AM, 14:00") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_time_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
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
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
                                imageVector = if (state.reminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (state.reminderSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column {
                                Text(
                                    text = "Exact Timed Call Reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Rings via AlarmManager with meeting link",
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
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Alert Timing (Before Call):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                val timings = listOf(
                                    0 to "At call time",
                                    15 to "15 min before",
                                    30 to "30 min before",
                                    60 to "1 hour before",
                                    1440 to "1 day before"
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
                            OutlinedButton(
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
                                    Toast.makeText(context, "🔔 Test interview notification fired!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_notification_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔔 Preview / Test Notification Sound")
                            }
                        }
                    }
                }
            }
            }

            // Section: Recruiter & Referral Contact
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recruiter_contact_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactMail,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Recruiter & Referral Contact",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Recruiter Name
                    OutlinedTextField(
                        value = state.recruiterName,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnRecruiterNameChange(it)) },
                        label = { Text("Recruiter / Contact Name") },
                        placeholder = { Text("e.g. Sarah Jenkins") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recruiter_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Recruiter Email
                    OutlinedTextField(
                        value = state.recruiterEmail,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnRecruiterEmailChange(it)) },
                        label = { Text("Recruiter Email") },
                        placeholder = { Text("e.g. sjenkins@company.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recruiter_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Phone / Profile Link
                    OutlinedTextField(
                        value = state.recruiterPhone,
                        onValueChange = { onEvent(AddEditJobUiEvent.OnRecruiterPhoneChange(it)) },
                        label = { Text("Phone or LinkedIn Profile") },
                        placeholder = { Text("e.g. linkedin.com/in/sarah-recruiter") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recruiter_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Referral Source Selector
                    Text(
                        text = "Referral Source",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val sources = listOf("LinkedIn", "Employee Referral", "Company Website", "Recruiter Outreach", "Job Board", "Other")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        sources.forEach { source ->
                            DateQuickChip(
                                label = source,
                                isSelected = state.referralSource == source,
                                onClick = {
                                    val newSource = if (state.referralSource == source) "" else source
                                    onEvent(AddEditJobUiEvent.OnReferralSourceChange(newSource))
                                }
                            )
                        }
                    }
                }
            }

            // General Notes / Details Editor
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(AddEditJobUiEvent.OnNotesChange(it)) },
                label = { Text("General Notes & Details (Optional)") },
                placeholder = { Text("Preparation details, interview feedback, compensation breakdown...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
