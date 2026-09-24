package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.domain.model.StatusSummary
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dashboard.DashboardUiState
import com.example.presentation.detail.AddEditJobScreen
import com.example.presentation.detail.AddEditJobUiEvent
import com.example.presentation.detail.AddEditJobUiState
import com.example.ui.theme.JobTrackerTheme
import com.example.util.JobDraft
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JobTrackerComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_displaysJobCardsAndSummary() {
        val sampleJob = JobApplication(
            id = 1L,
            companyName = "Acme Corp",
            jobTitle = "Staff Android Developer",
            dateApplied = "2026-09-20",
            status = JobStatus.APPLIED,
            salary = "$175k"
        )
        val state = DashboardUiState(
            applications = listOf(sampleJob),
            summary = StatusSummary(total = 1, applied = 1),
            isLoading = false
        )

        composeTestRule.setContent {
            JobTrackerTheme {
                DashboardScreen(
                    state = state,
                    onEvent = {},
                    onNavigateToAdd = {},
                    onNavigateToEdit = {},
                    onOpenAiTools = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Staff Android Developer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Acme Corp").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_job_fab").assertIsDisplayed()
    }

    @Test
    fun dashboard_pipelineCardCollapsibleDropdownTogglesChips() {
        val state = DashboardUiState(
            summary = StatusSummary(total = 3, applied = 1, interviewing = 1, offer = 1),
            isLoading = false
        )

        composeTestRule.setContent {
            JobTrackerTheme {
                DashboardScreen(
                    state = state,
                    onEvent = {},
                    onNavigateToAdd = {},
                    onNavigateToEdit = {},
                    onOpenAiTools = {}
                )
            }
        }

        // Header and compact status summary bar displayed by default (collapsed)
        composeTestRule.onNodeWithTag("pipeline_card_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("compact_status_summary_bar").assertIsDisplayed()

        // Tap card header to toggle expansion
        composeTestRule.onNodeWithTag("pipeline_card_header").performClick()

        // Full status filter chips are revealed
        composeTestRule.onNodeWithTag("status_chips_flow_row").assertIsDisplayed()
        composeTestRule.onNodeWithText("Applied").assertIsDisplayed()
        composeTestRule.onNodeWithText("Interviewing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Offer").assertIsDisplayed()

        // Tap card header again to collapse
        composeTestRule.onNodeWithTag("pipeline_card_header").performClick()
        composeTestRule.onNodeWithTag("compact_status_summary_bar").assertIsDisplayed()
    }

    @Test
    fun addEditJobScreen_displaysInputFields() {
        val state = AddEditJobUiState(
            companyName = "OpenAI",
            jobTitle = "Research Engineer",
            dateApplied = "2026-09-20"
        )

        composeTestRule.setContent {
            JobTrackerTheme {
                AddEditJobScreen(
                    state = state,
                    onEvent = {},
                    onNavigateBack = {},
                    onOpenAiTools = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("company_name_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("job_title_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_job_button").assertIsDisplayed()
    }

    @Test
    fun dashboard_displaysDraftSectionWhenDraftExists() {
        val draft = JobDraft(
            companyName = "Stripe",
            jobTitle = "Backend Architect",
            salary = "$190,000",
            notes = "Completed screen round"
        )

        composeTestRule.setContent {
            JobTrackerTheme {
                com.example.presentation.dashboard.components.DashboardDraftSection(
                    draft = draft,
                    onResumeDraft = {},
                    onDiscardDraft = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("dashboard_draft_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Backend Architect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stripe").assertIsDisplayed()
        composeTestRule.onNodeWithTag("resume_draft_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("discard_draft_button").assertIsDisplayed()
    }
}
