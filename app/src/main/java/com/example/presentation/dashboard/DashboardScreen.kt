package com.example.presentation.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.di.AppContainer
import com.example.domain.analytics.StaleApplications
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.localization.LocalAppStrings
import com.example.presentation.components.LanguageToggleButton
import com.example.presentation.components.RetroButton
import com.example.presentation.components.RetroButtonStyle
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroTopBarRule
import com.example.presentation.components.hardShadow
import com.example.presentation.dashboard.components.DashboardDraftSection
import com.example.presentation.dashboard.components.DashboardSearchBar
import com.example.presentation.dashboard.components.EmptyState
import com.example.presentation.dashboard.components.JobCard
import com.example.presentation.dashboard.components.StatusSummarySection
import com.example.presentation.dashboard.components.UpcomingInterviewsSection
import com.example.presentation.export.ExportBackupBottomSheet
import com.example.presentation.insights.InsightsBottomSheet
import com.example.presentation.offers.OfferComparisonBottomSheet
import com.example.util.AppLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onOpenAiTools: () -> Unit,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onLanguageChange: (AppLanguage) -> Unit = {},
    appContainer: AppContainer? = null,
    onFollowUp: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val exportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showExportSheet by remember { mutableStateOf(false) }
    val insightsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showInsightsSheet by remember { mutableStateOf(false) }
    val offersSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showOffersSheet by remember { mutableStateOf(false) }
    var isPipelineExpanded by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    val topApplicationId = state.applications.firstOrNull()?.id
    val applicationsCount = state.applications.size

    // Auto scroll to top when new applications are added so the newest record is viewed first
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
                actionLabel = if (isDeletionMessage) strings.undo else null,
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

    val allApplications = state.rawApplications.ifEmpty { state.applications }
    val offers = allApplications.filter { it.status == JobStatus.OFFER }

    if (showInsightsSheet) {
        InsightsBottomSheet(
            applications = allApplications,
            sheetState = insightsSheetState,
            onDismiss = {
                scope.launch { insightsSheetState.hide() }.invokeOnCompletion { showInsightsSheet = false }
            }
        )
    }

    if (showOffersSheet && offers.size >= 2) {
        OfferComparisonBottomSheet(
            offers = offers,
            sheetState = offersSheetState,
            onDismiss = {
                scope.launch { offersSheetState.hide() }.invokeOnCompletion { showOffersSheet = false }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("dashboard_screen"),
        containerColor = Color.Transparent,
        topBar = {
            DashboardMasthead(
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                onOpenAiTools = onOpenAiTools,
                onOpenInsights = { showInsightsSheet = true },
                onOpenExport = { showExportSheet = true }
            )
        },
        floatingActionButton = {
            RetroButton(
                text = strings.addApplication,
                onClick = onNavigateToAdd,
                icon = Icons.Default.Add,
                style = RetroButtonStyle.Primary,
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
                .padding(horizontal = 14.dp)
        ) {
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
                modifier = Modifier.padding(top = 14.dp, bottom = 12.dp)
            )

            if (state.activeDraft != null && (state.isDraftFilterSelected || (state.selectedStatus == null && state.searchQuery.isBlank()))) {
                DashboardDraftSection(
                    draft = state.activeDraft,
                    onResumeDraft = onNavigateToAdd,
                    onDiscardDraft = { onEvent(DashboardUiEvent.OnDiscardDraft) },
                    isHighlighted = state.isDraftFilterSelected,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (state.upcomingInterviews.isNotEmpty() && !state.isDraftFilterSelected) {
                UpcomingInterviewsSection(
                    interviews = state.upcomingInterviews,
                    onSelectJob = onNavigateToEdit,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            if (offers.size >= 2 && !state.isDraftFilterSelected) {
                RetroButton(
                    text = strings.compareOffersButton(offers.size),
                    onClick = { showOffersSheet = true },
                    icon = Icons.Default.CompareArrows,
                    style = RetroButtonStyle.Secondary,
                    compact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("compare_offers_button")
                )
            }

            if (!state.isDraftFilterSelected) {
                DashboardSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(DashboardUiEvent.OnSearchQueryChange(it)) },
                    selectedSort = state.sortOption,
                    onSortSelected = { onEvent(DashboardUiEvent.OnSortOptionSelected(it)) },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

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
                            strokeWidth = 3.dp,
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
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.applications,
                            key = { it.id }
                        ) { application ->
                            JobCard(
                                application = application,
                                onClick = { onNavigateToEdit(application.id) },
                                onDelete = { onEvent(DashboardUiEvent.OnDeleteApplication(application)) },
                                staleDays = StaleApplications.daysWithoutUpdate(application),
                                onFollowUp = { onFollowUp(application.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMasthead(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenAiTools: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenExport: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    val logoShape = RoundedCornerShape(4.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        // Printed edge stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(colors.primary)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .hardShadow(3.dp, colors.outline.copy(alpha = 0.45f), logoShape)
                    .clip(logoShape)
                    .background(colors.primary)
                    .border(2.dp, colors.outline, logoShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(colors.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.appSubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Control deck: actions live on their own printed strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceVariant)
                .border(width = 2.dp, color = colors.outline)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroButton(
                text = strings.aiTools,
                onClick = onOpenAiTools,
                icon = Icons.Default.AutoAwesome,
                style = RetroButtonStyle.Primary,
                compact = true,
                modifier = Modifier.testTag("dashboard_ai_tools_button")
            )

            Spacer(modifier = Modifier.weight(1f))

            LanguageToggleButton(
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                modifier = Modifier
                    .testTag("language_toggle_button")
            )

            Spacer(modifier = Modifier.width(8.dp))

            RetroIconButton(
                icon = Icons.Default.BarChart,
                contentDescription = strings.insightsButtonDesc,
                onClick = onOpenInsights,
                backgroundColor = colors.surface,
                size = 40.dp,
                modifier = Modifier.testTag("dashboard_insights_button")
            )

            Spacer(modifier = Modifier.width(8.dp))

            RetroIconButton(
                icon = Icons.Default.FileDownload,
                contentDescription = strings.exportAndBackup,
                onClick = onOpenExport,
                backgroundColor = colors.surface,
                size = 40.dp,
                modifier = Modifier.testTag("dashboard_export_button")
            )
        }

        RetroTopBarRule()
    }
}
