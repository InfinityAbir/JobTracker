package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.JobStatus
import com.example.presentation.JobViewModelFactory
import com.example.presentation.ai.AiToolsBottomSheet
import com.example.presentation.ai.AiToolsViewModel
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dashboard.DashboardViewModel
import com.example.presentation.detail.AddEditJobScreen
import com.example.presentation.detail.AddEditJobUiEvent
import com.example.presentation.detail.AddEditJobViewModel
import com.example.presentation.navigation.Screen
import com.example.ui.theme.JobTrackerTheme
import com.example.util.JobDraft
import com.example.util.JobDraftManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as JobTrackerApp).container

        setContent {
            JobTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    JobTrackerNavigation(appContainer = appContainer)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobTrackerNavigation(appContainer: com.example.di.AppContainer) {
    val context = LocalContext.current
    val navController = rememberNavController()
    var showAiBottomSheet by remember { mutableStateOf(false) }
    var activeJobIdForAi by remember { mutableStateOf(0L) }
    var isAiFromExistingForm by remember { mutableStateOf(false) }
    var pendingAutoFillCallback by remember { mutableStateOf<((com.example.data.remote.ParsedJobData) -> Unit)?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(
            route = Screen.Dashboard.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            }
        ) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = JobViewModelFactory(appContainer)
            )
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

            DashboardScreen(
                state = uiState,
                onEvent = dashboardViewModel::onEvent,
                onNavigateToAdd = {
                    navController.navigate(Screen.AddJob.route)
                },
                onNavigateToEdit = { jobId ->
                    navController.navigate(Screen.EditJob.createRoute(jobId))
                },
                onOpenAiTools = {
                    activeJobIdForAi = 0L
                    isAiFromExistingForm = false
                    pendingAutoFillCallback = { parsed ->
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val newDraft = JobDraft(
                            companyName = parsed.companyName,
                            jobTitle = parsed.jobTitle,
                            salary = parsed.salary,
                            notes = parsed.keyPoints,
                            dateApplied = today,
                            status = JobStatus.APPLIED
                        )
                        JobDraftManager.saveDraft(context, newDraft)
                        showAiBottomSheet = false
                        navController.navigate(Screen.AddJob.route)
                    }
                    showAiBottomSheet = true
                },
                appContainer = appContainer
            )
        }

        composable(
            route = Screen.AddJob.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            val addEditJobViewModel: AddEditJobViewModel = viewModel(
                factory = JobViewModelFactory(appContainer, jobId = 0L)
            )
            val uiState by addEditJobViewModel.uiState.collectAsStateWithLifecycle()

            AddEditJobScreen(
                state = uiState,
                onEvent = addEditJobViewModel::onEvent,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenAiTools = {
                    activeJobIdForAi = 0L
                    isAiFromExistingForm = true
                    pendingAutoFillCallback = { parsed ->
                        addEditJobViewModel.onEvent(
                            AddEditJobUiEvent.OnApplyParsedJob(
                                companyName = parsed.companyName,
                                jobTitle = parsed.jobTitle,
                                salary = parsed.salary,
                                notes = parsed.keyPoints
                            )
                        )
                        showAiBottomSheet = false
                    }
                    showAiBottomSheet = true
                }
            )
        }

        composable(
            route = Screen.EditJob.route,
            arguments = listOf(
                navArgument("jobId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong("jobId") ?: 0L
            val addEditJobViewModel: AddEditJobViewModel = viewModel(
                key = "edit_job_$jobId",
                factory = JobViewModelFactory(appContainer, jobId = jobId)
            )
            val uiState by addEditJobViewModel.uiState.collectAsStateWithLifecycle()

            AddEditJobScreen(
                state = uiState,
                onEvent = addEditJobViewModel::onEvent,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenAiTools = {
                    activeJobIdForAi = jobId
                    isAiFromExistingForm = true
                    pendingAutoFillCallback = { parsed ->
                        addEditJobViewModel.onEvent(
                            AddEditJobUiEvent.OnApplyParsedJob(
                                companyName = parsed.companyName,
                                jobTitle = parsed.jobTitle,
                                salary = parsed.salary,
                                notes = parsed.keyPoints
                            )
                        )
                        showAiBottomSheet = false
                    }
                    showAiBottomSheet = true
                }
            )
        }
    }

    if (showAiBottomSheet) {
        val aiViewModel: AiToolsViewModel = viewModel(
            key = "ai_tools_${activeJobIdForAi}",
            factory = JobViewModelFactory(appContainer, jobId = activeJobIdForAi)
        )
        val aiState by aiViewModel.uiState.collectAsStateWithLifecycle()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        AiToolsBottomSheet(
            state = aiState,
            onEvent = aiViewModel::onEvent,
            sheetState = sheetState,
            onDismiss = { showAiBottomSheet = false },
            onApplyParsedData = pendingAutoFillCallback,
            isFromExistingForm = isAiFromExistingForm
        )
    }
}

