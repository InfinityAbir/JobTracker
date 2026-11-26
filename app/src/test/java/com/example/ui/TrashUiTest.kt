package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.domain.model.StatusSummary
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dashboard.DashboardUiEvent
import com.example.presentation.dashboard.DashboardUiState
import com.example.ui.theme.JobTrackerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class TrashUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val live = JobApplication(
        id = 1L, companyName = "Acme Corp", jobTitle = "Staff Android Developer",
        dateApplied = "2026-09-20", status = JobStatus.APPLIED
    )
    private val trashedA = JobApplication(
        id = 2L, companyName = "Globex", jobTitle = "Platform Engineer",
        dateApplied = "2026-09-01", status = JobStatus.REJECTED,
        deletedAt = System.currentTimeMillis()
    )
    private val trashedB = JobApplication(
        id = 3L, companyName = "Initech", jobTitle = "Backend Engineer",
        dateApplied = "2026-08-01", status = JobStatus.INTERVIEWING,
        deletedAt = System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000
    )

    /** The dashboard also emits a draft refresh on resume; only trash-related events matter here. */
    private fun MutableList<DashboardUiEvent>.trashEvents(): List<DashboardUiEvent> =
        filter { it !is DashboardUiEvent.OnRefreshDraft }

    private fun show(trashed: List<JobApplication>, events: MutableList<DashboardUiEvent>) {
        composeTestRule.setContent {
            JobTrackerTheme {
                DashboardScreen(
                    state = DashboardUiState(
                        applications = listOf(live),
                        summary = StatusSummary(total = 1, applied = 1),
                        trashedApplications = trashed,
                        isLoading = false
                    ),
                    onEvent = { events.add(it) },
                    onNavigateToAdd = {},
                    onNavigateToEdit = {},
                    onOpenAiTools = {}
                )
            }
        }
    }

    @Test
    fun trashButton_hiddenWhenTrashIsEmpty() {
        show(emptyList(), mutableListOf())
        composeTestRule.onNodeWithTag("open_trash_button").assertDoesNotExist()
    }

    @Test
    fun trashButton_opensSheetListingDeletedApplications() {
        show(listOf(trashedA, trashedB), mutableListOf())

        composeTestRule.onNodeWithTag("open_trash_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Trash (2)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_trash_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("trash_bottom_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trash_item_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trash_item_3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deleted today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deleted 3 days ago").assertIsDisplayed()
        composeTestRule.onNodeWithTag("empty_trash_button").assertIsDisplayed()
    }

    @Test
    fun restore_sendsEventWithoutConfirmation() {
        val events = mutableListOf<DashboardUiEvent>()
        show(listOf(trashedA), events)
        composeTestRule.onNodeWithTag("open_trash_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("trash_restore_button_2").performClick()

        assertEquals(listOf<DashboardUiEvent>(DashboardUiEvent.OnRestoreFromTrash(trashedA)), events.trashEvents())
    }

    @Test
    fun deleteForever_needsConfirmation_andCancelDoesNothing() {
        val events = mutableListOf<DashboardUiEvent>()
        show(listOf(trashedA), events)
        composeTestRule.onNodeWithTag("open_trash_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("trash_delete_forever_button_2").performClick()
        composeTestRule.onNodeWithTag("delete_forever_dialog").assertIsDisplayed()
        // Nothing is deleted until the user confirms
        assertTrue(events.trashEvents().isEmpty())

        composeTestRule.onNodeWithTag("delete_forever_cancel_button").performClick()
        composeTestRule.onNodeWithTag("delete_forever_dialog").assertDoesNotExist()
        assertTrue(events.trashEvents().isEmpty())

        composeTestRule.onNodeWithTag("trash_delete_forever_button_2").performClick()
        composeTestRule.onNodeWithTag("delete_forever_confirm_button").performClick()
        assertEquals(listOf<DashboardUiEvent>(DashboardUiEvent.OnDeleteForever(trashedA)), events.trashEvents())
    }

    @Test
    fun emptyTrash_needsConfirmation_thenSendsSingleEvent() {
        val events = mutableListOf<DashboardUiEvent>()
        show(listOf(trashedA, trashedB), events)
        composeTestRule.onNodeWithTag("open_trash_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("empty_trash_button").performClick()
        composeTestRule.onNodeWithTag("empty_trash_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Permanently delete all 2 applications in the trash? This cannot be undone.")
            .assertIsDisplayed()
        assertTrue(events.trashEvents().isEmpty())

        composeTestRule.onNodeWithTag("empty_trash_confirm_button").performClick()
        assertEquals(listOf<DashboardUiEvent>(DashboardUiEvent.OnEmptyTrash), events.trashEvents())
    }
}
