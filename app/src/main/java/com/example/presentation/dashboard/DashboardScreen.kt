package com.example.presentation.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import com.example.presentation.components.RetroFab
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
import com.example.domain.analytics.upcomingInterviews
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val FabBottomPadding = 8.dp
/** Gap Material3 Scaffold leaves between the FAB and the bottom of the content area. */
private val ScaffoldFabSpacing = 16.dp
private val FabListBreathingRoom = 16.dp
private val MinListBottomPadding = 100.dp

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

    // Small screens or enlarged text: the header would eat a third of the screen if pinned,
    // so it becomes the first item of the list and scrolls away with the content.
    val compactChrome = LocalConfiguration.current.screenWidthDp < 360 ||
        LocalDensity.current.fontScale > 1.15f
    val pageEdge = Modifier.padding(horizontal = 14.dp)

    // Measured FAB height (includes its own bottom padding). Drives list bottom padding so the
    // last card can always scroll fully above the FAB, whatever the font scale or screen size.
    var fabHeightPx by remember { mutableIntStateOf(0) }
    val listBottomPadding = with(LocalDensity.current) {
        val clearance = fabHeightPx.toDp() + ScaffoldFabSpacing + FabListBreathingRoom
        maxOf(clearance, MinListBottomPadding)
    }

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

    // Clock that ticks once a minute so a finished interview drops off the Upcoming strip on its own.
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(60_000L)
        }
    }
    val upcomingInterviews = remember(state.upcomingInterviews, nowMillis) {
        state.upcomingInterviews.upcomingInterviews(nowMillis)
    }

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
            if (!compactChrome) {
                DashboardMasthead(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    onOpenAiTools = onOpenAiTools,
                    onOpenInsights = { showInsightsSheet = true },
                    onOpenExport = { showExportSheet = true }
                )
            }
        },
        // Scaffold already lifts the FAB above the system bars (gesture pill or 3-button bar)
        // and keeps it at the end edge, so only its measured height matters for list padding.
        floatingActionButton = {
            RetroFab(
                text = strings.addApplication,
                onClick = onNavigateToAdd,
                icon = Icons.Default.Add,
                modifier = Modifier
                    .onSizeChanged { fabHeightPx = it.height }
                    .testTag("add_job_fab")
                    .padding(bottom = FabBottomPadding)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        // One scrolling list for the whole dashboard. Summary, draft, interviews and search used to be
        // pinned above a fixed-height list, which squeezed the applications on shorter screens.
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("job_applications_list"),
            contentPadding = PaddingValues(top = if (compactChrome) 0.dp else 14.dp, bottom = listBottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (compactChrome) {
                item(key = "masthead") {
                    DashboardMasthead(
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange,
                        onOpenAiTools = onOpenAiTools,
                        onOpenInsights = { showInsightsSheet = true },
                        onOpenExport = { showExportSheet = true },
                        pinned = false
                    )
                }
            }

            item(key = "summary") {
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
                    modifier = pageEdge
                )
            }

            if (state.activeDraft != null && (state.isDraftFilterSelected || (state.selectedStatus == null && state.searchQuery.isBlank()))) {
                item(key = "draft") {
                    DashboardDraftSection(
                        draft = state.activeDraft,
                        onResumeDraft = onNavigateToAdd,
                        onDiscardDraft = { onEvent(DashboardUiEvent.OnDiscardDraft) },
                        isHighlighted = state.isDraftFilterSelected,
                        modifier = pageEdge
                    )
                }
            }

            if (upcomingInterviews.isNotEmpty() && !state.isDraftFilterSelected) {
                item(key = "interviews") {
                    UpcomingInterviewsSection(
                        interviews = upcomingInterviews,
                        onSelectJob = onNavigateToEdit,
                        modifier = pageEdge
                    )
                }
            }

            if (offers.size >= 2 && !state.isDraftFilterSelected) {
                item(key = "compare_offers") {
                    RetroButton(
                        text = strings.compareOffersButton(offers.size),
                        onClick = { showOffersSheet = true },
                        icon = Icons.Default.CompareArrows,
                        style = RetroButtonStyle.Secondary,
                        compact = true,
                        modifier = pageEdge
                            .fillMaxWidth()
                            .testTag("compare_offers_button")
                    )
                }
            }

            if (!state.isDraftFilterSelected) {
                item(key = "search") {
                    DashboardSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(DashboardUiEvent.OnSearchQueryChange(it)) },
                        selectedSort = state.sortOption,
                        onSortSelected = { onEvent(DashboardUiEvent.OnSortOptionSelected(it)) },
                        modifier = pageEdge
                    )
                }
            }

            when {
                state.isLoading -> item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.testTag("loading_indicator")
                        )
                    }
                }

                state.isDraftFilterSelected -> {
                    if (state.activeDraft == null) {
                        item(key = "empty_draft") {
                            EmptyState(
                                isSearching = true,
                                onAddApplication = onNavigateToAdd,
                                onClearSearch = {
                                    onEvent(DashboardUiEvent.OnDraftFilterToggled)
                                }
                            )
                        }
                    }
                }

                state.applications.isEmpty() -> item(key = "empty") {
                    EmptyState(
                        isSearching = state.searchQuery.isNotBlank() || state.selectedStatus != null,
                        onAddApplication = onNavigateToAdd,
                        onClearSearch = {
                            onEvent(DashboardUiEvent.OnSearchQueryChange(""))
                            onEvent(DashboardUiEvent.OnStatusFilterSelected(null))
                        }
                    )
                }

                else -> {
                    // Section header: tells the user where the list starts and how many records match.
                    item(key = "section_header") {
                        Row(
                            modifier = pageEdge.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.applicationsSectionTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = state.applications.size.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("applications_count")
                            )
                        }
                    }

                    items(
                        items = state.applications,
                        key = { it.id }
                    ) { application ->
                        JobCard(
                            application = application,
                            onClick = { onNavigateToEdit(application.id) },
                            onDelete = { onEvent(DashboardUiEvent.OnDeleteApplication(application)) },
                            staleDays = StaleApplications.daysWithoutUpdate(application),
                            onFollowUp = { onFollowUp(application.id) },
                            modifier = pageEdge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardMasthead(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenAiTools: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenExport: () -> Unit,
    pinned: Boolean = true
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    val logoShape = RoundedCornerShape(4.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .then(if (pinned) Modifier.statusBarsPadding() else Modifier)
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
        // FlowRow keeps AI Tools left and the utilities right on normal screens, and wraps the
        // utilities onto a second line on very narrow ones instead of clipping them.
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceVariant)
                .border(width = 1.dp, color = colors.outlineVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RetroButton(
                text = strings.aiTools,
                onClick = onOpenAiTools,
                icon = Icons.Default.AutoAwesome,
                style = RetroButtonStyle.Primary,
                compact = true,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .testTag("dashboard_ai_tools_button")
            )

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageToggleButton(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    modifier = Modifier
                        .testTag("language_toggle_button")
                )

                RetroIconButton(
                    icon = Icons.Default.BarChart,
                    contentDescription = strings.insightsButtonDesc,
                    onClick = onOpenInsights,
                    backgroundColor = colors.surface,
                    size = 40.dp,
                    modifier = Modifier.testTag("dashboard_insights_button")
                )

                RetroIconButton(
                    icon = Icons.Default.FileDownload,
                    contentDescription = strings.exportAndBackup,
                    onClick = onOpenExport,
                    backgroundColor = colors.surface,
                    size = 40.dp,
                    modifier = Modifier.testTag("dashboard_export_button")
                )
            }
        }

        RetroTopBarRule()
    }
}
