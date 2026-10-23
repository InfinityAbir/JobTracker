package com.example.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroButton
import com.example.presentation.components.RetroButtonStyle
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroStamp
import com.example.presentation.components.RetroTag
import com.example.presentation.components.hardShadow
import com.example.presentation.components.onAccent
import com.example.presentation.components.retroDraftColors
import com.example.util.JobDraft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardDraftSection(
    draft: JobDraft,
    onResumeDraft: () -> Unit,
    onDiscardDraft: () -> Unit,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    val draftColors = retroDraftColors()
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }

    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(4.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = colors.error
                )
            },
            title = {
                Text(
                    text = strings.discardDraftDialogTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.discardDraftDialogMessage(draft.companyName.ifBlank { "Untitled Company" }),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                RetroButton(
                    text = strings.discardDraft,
                    onClick = {
                        showDiscardConfirmDialog = false
                        onDiscardDraft()
                    },
                    style = RetroButtonStyle.Danger,
                    compact = true,
                    modifier = Modifier.testTag("confirm_discard_draft_button")
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_discard_draft_button")
                ) {
                    Text(
                        text = strings.keepDraft,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary
                    )
                }
            },
            modifier = Modifier.testTag("discard_draft_confirmation_dialog")
        )
    }

    val relativeTime = remember(draft.savedAtTimestamp, strings) {
        val diffMs = System.currentTimeMillis() - draft.savedAtTimestamp
        val diffMin = diffMs / (1000 * 60)
        val diffHours = diffMin / 60
        val diffDays = diffHours / 24
        when {
            diffMin < 1 -> strings.savedJustNow
            diffMin < 60 -> strings.savedMinutesAgo(diffMin)
            diffHours < 24 -> strings.savedHoursAgo(diffHours)
            diffDays < 7 -> strings.savedDaysAgo(diffDays)
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                strings.savedOnDate(sdf.format(Date(draft.savedAtTimestamp)))
            }
        }
    }

    RetroPanel(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_draft_card"),
        onClick = onResumeDraft,
        backgroundColor = draftColors.background,
        borderColor = draftColors.ink,
        shadowColor = draftColors.ink.copy(alpha = 0.4f),
        shadowOffset = if (isHighlighted) 4.dp else 3.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        // Left perforated edge strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(draftColors.ink)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(13.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroStamp(
                            text = strings.inProgressDraft,
                            color = draftColors.ink,
                            rotation = -1.5f
                        )
                        if (isHighlighted) {
                            Text(
                                text = strings.filtered,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = draftColors.ink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = relativeTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = draft.jobTitle.ifBlank { "Untitled Position" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = draft.companyName.ifBlank { "Company name not set yet" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (draft.companyName.isNotBlank()) draftColors.ink else colors.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val infoItems = buildList {
                    if (draft.salary.isNotBlank()) add("PAY · ${draft.salary}")
                    if (draft.interviewDate.isNotBlank()) add("INT · ${draft.interviewDate}")
                    if (draft.recruiterName.isNotBlank()) add("REF · ${draft.recruiterName}")
                    if (draft.notes.isNotBlank()) add("NOTES ON FILE")
                }

                if (infoItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        infoItems.take(2).forEach { info ->
                            RetroTag(
                                text = info,
                                color = colors.onSurfaceVariant,
                                borderColor = draftColors.ink.copy(alpha = 0.45f),
                                backgroundColor = colors.surface.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroButton(
                        text = strings.resumeEditDraft,
                        onClick = onResumeDraft,
                        icon = Icons.Default.Edit,
                        compact = true,
                        backgroundColor = draftColors.ink,
                        contentColor = onAccent(draftColors.ink),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("resume_draft_button")
                    )

                    Box(
                        modifier = Modifier
                            .hardShadow(2.dp, colors.outline.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.surface)
                            .border(1.5.dp, draftColors.ink.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
                    ) {
                        TextButton(
                            onClick = { showDiscardConfirmDialog = true },
                            modifier = Modifier.testTag("discard_draft_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = strings.discardDraft,
                                modifier = Modifier.size(15.dp),
                                tint = colors.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.discard,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.error
                            )
                        }
                    }
                }
            }
        }
    }
}
