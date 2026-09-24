package com.example.presentation.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.di.AppContainer
import com.example.domain.model.JobApplication
import com.example.presentation.dashboard.components.DashboardDraftSection
import com.example.presentation.dashboard.components.DashboardSearchBar
import com.example.presentation.dashboard.components.EmptyState
import com.example.presentation.dashboard.components.JobCard
import com.example.presentation.dashboard.components.StatusSummarySection
import com.example.presentation.dashboard.components.UpcomingInterviewsSection
import com.example.presentation.export.ExportBackupBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onOpenAiTools: () -> Unit,
    appContainer: AppContainer? = null,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val exportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showExportSheet by remember { mutableStateOf(false) }
    var isPipelineExpanded by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    val topApplicationId = state.applications.firstOrNull()?.id
    val applicationsCount = state.applications.size

    // Auto scroll to top when new applications are added or list updates so newest application is viewed first
    LaunchedEffect(topApplicationId, applicationsCount) {
        if (applicationsCount > 0) {
            listState.animateScrollToItem(0)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onEvent(DashboardUiEvent.OnRefreshDraft)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Intercept back press when keyboard is visible to dismiss keyboard first
    BackHandler(enabled = isImeVisible) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            val isDeletionMessage = message.contains("deleted", ignoreCase = true)
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (isDeletionMessage) "Undo" else null,
                duration = SnackbarDuration.Short
            )
            if (isDeletionMessage && result == SnackbarResult.ActionPerformed) {
                onEvent(DashboardUiEvent.OnRestoreApplication(JobApplication(companyName = "", jobTitle = "", dateApplied = "")))
            }
            onEvent(DashboardUiEvent.OnDismissMessage)
        }
    }

    if (showExportSheet) {
        ExportBackupBottomSheet(
            applications = state.rawApplications.ifEmpty { state.applications },
            sheetState = exportSheetState,
            appContainer = appContainer,
            onDismiss = {
                scope.launch { exportSheetState.hide() }.invokeOnCompletion {
                    showExportSheet = false
                }
            }
        )
    }

    // Gradient background for top header
    val topGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        modifier = modifier.testTag("dashboard_screen"),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topGradient)
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Career Tracker & Interview Hub",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Prominent Export & Backup Button
                        Surface(
                            onClick = { showExportSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .testTag("dashboard_export_button")
                                .padding(end = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export & Backup",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Prominent AI Assistant Button with Bright Gold & Deep Navy contrast
                        Surface(
                            onClick = onOpenAiTools,
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .testTag("dashboard_ai_tools_button")
                                .padding(end = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Job Assistant",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "AI Tools",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Application",
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(
                        text = "Add Application",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .testTag("add_job_fab")
                    .padding(bottom = 8.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Pipeline Summary Section with Collapsible Dropdown & Active Chips
            StatusSummarySection(
                summary = state.summary,
                selectedStatus = state.selectedStatus,
                onStatusSelected = { status ->
                    onEvent(DashboardUiEvent.OnStatusFilterSelected(status))
                },
                isDraftSelected = state.isDraftFilterSelected,
                onDraftSelected = {
                    onEvent(DashboardUiEvent.OnDraftFilterToggled)
                },
                isExpanded = isPipelineExpanded,
                onToggleExpanded = {
                    isPipelineExpanded = !isPipelineExpanded
                },
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // In-progress draft application section (shown when a draft exists and not hidden by other status filters)
            if (state.activeDraft != null && (state.isDraftFilterSelected || (state.selectedStatus == null && state.searchQuery.isBlank()))) {
                DashboardDraftSection(
                    draft = state.activeDraft,
                    onResumeDraft = onNavigateToAdd,
                    onDiscardDraft = { onEvent(DashboardUiEvent.OnDiscardDraft) },
                    isHighlighted = state.isDraftFilterSelected,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Upcoming Interviews widget (if scheduled and not filtering for drafts only)
            if (state.upcomingInterviews.isNotEmpty() && !state.isDraftFilterSelected) {
                UpcomingInterviewsSection(
                    interviews = state.upcomingInterviews,
                    onSelectJob = onNavigateToEdit,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Search Bar & Sort Dropdown (hidden if draft-only filter is active for maximum focus)
            if (!state.isDraftFilterSelected) {
                DashboardSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(DashboardUiEvent.OnSearchQueryChange(it)) },
                    selectedSort = state.sortOption,
                    onSortSelected = { onEvent(DashboardUiEvent.OnSortOptionSelected(it)) },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Content List / Empty / Loading State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("loading_indicator")
                        )
                    }
                } else if (state.isDraftFilterSelected) {
                    if (state.activeDraft == null) {
                        EmptyState(
                            isSearching = true,
                            onAddApplication = onNavigateToAdd,
                            onClearSearch = {
                                onEvent(DashboardUiEvent.OnDraftFilterToggled)
                            }
                        )
                    }
                } else if (state.applications.isEmpty()) {
                    EmptyState(
                        isSearching = state.searchQuery.isNotBlank() || state.selectedStatus != null,
                        onAddApplication = onNavigateToAdd,
                        onClearSearch = {
                            onEvent(DashboardUiEvent.OnSearchQueryChange(""))
                            onEvent(DashboardUiEvent.OnStatusFilterSelected(null))
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("job_applications_list"),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = state.applications,
                            key = { it.id }
                        ) { application ->
                            JobCard(
                                application = application,
                                onClick = { onNavigateToEdit(application.id) },
                                onDelete = { onEvent(DashboardUiEvent.OnDeleteApplication(application)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
