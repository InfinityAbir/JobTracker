package com.example.presentation.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.JobStatus
import com.example.domain.model.StatusSummary
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroStamp
import com.example.presentation.components.hardShadow
import com.example.presentation.components.onAccent
import com.example.presentation.components.retroDraftColors
import com.example.presentation.components.retroStatusColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusSummarySection(
    summary: StatusSummary,
    selectedStatus: JobStatus?,
    onStatusSelected: (JobStatus?) -> Unit,
    isDraftSelected: Boolean = false,
    onDraftSelected: () -> Unit = {},
    modifier: Modifier = Modifier,
    isExpanded: Boolean? = null,
    onToggleExpanded: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    var internalExpanded by remember { mutableStateOf(false) }
    val expanded = isExpanded ?: internalExpanded
    val toggleExpanded = onToggleExpanded ?: { internalExpanded = !internalExpanded }

    RetroPanel(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_summary_card"),
        backgroundColor = colors.surface,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .clickable(onClick = toggleExpanded)
                .testTag("pipeline_card_header")
                .padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Little ledger mark
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(colors.primary, RoundedCornerShape(2.dp))
                        .border(1.5.dp, colors.outline, RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "01",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = onAccent(colors.primary)
                    )
                }
                Text(
                    text = strings.pipelineTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetroStamp(
                    text = strings.totalCount(summary.total),
                    color = colors.primary
                )

                val rotationState by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 220),
                    label = "expand_arrow_rotation"
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(colors.surfaceVariant, RoundedCornerShape(2.dp))
                        .border(1.5.dp, colors.outline, RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse Application Pipeline" else "Expand Application Pipeline",
                        tint = colors.onSurface,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationState)
                            .testTag("pipeline_expand_button")
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CompactStatusSummaryBar(
                summary = summary,
                selectedStatus = selectedStatus,
                isDraftSelected = isDraftSelected,
                modifier = Modifier.clickable(onClick = toggleExpanded)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("status_chips_flow_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMetricPill(
                        label = strings.statusAll,
                        count = summary.total,
                        accentColor = colors.primary,
                        isSelected = selectedStatus == null && !isDraftSelected,
                        onClick = { onStatusSelected(null) }
                    )

                    SummaryMetricPill(
                        label = strings.statusDraft,
                        count = summary.drafts,
                        accentColor = retroDraftColors().ink,
                        isSelected = isDraftSelected,
                        onClick = onDraftSelected
                    )

                    SummaryMetricPill(
                        label = strings.statusApplied,
                        count = summary.applied,
                        accentColor = retroStatusColors(JobStatus.APPLIED).ink,
                        isSelected = selectedStatus == JobStatus.APPLIED && !isDraftSelected,
                        onClick = {
                            onStatusSelected(if (selectedStatus == JobStatus.APPLIED && !isDraftSelected) null else JobStatus.APPLIED)
                        }
                    )

                    SummaryMetricPill(
                        label = strings.statusInterviewing,
                        count = summary.interviewing,
                        accentColor = retroStatusColors(JobStatus.INTERVIEWING).ink,
                        isSelected = selectedStatus == JobStatus.INTERVIEWING && !isDraftSelected,
                        onClick = {
                            onStatusSelected(if (selectedStatus == JobStatus.INTERVIEWING && !isDraftSelected) null else JobStatus.INTERVIEWING)
                        }
                    )

                    SummaryMetricPill(
                        label = strings.statusOffer,
                        count = summary.offer,
                        accentColor = retroStatusColors(JobStatus.OFFER).ink,
                        isSelected = selectedStatus == JobStatus.OFFER && !isDraftSelected,
                        onClick = {
                            onStatusSelected(if (selectedStatus == JobStatus.OFFER && !isDraftSelected) null else JobStatus.OFFER)
                        }
                    )

                    SummaryMetricPill(
                        label = strings.statusRejected,
                        count = summary.rejected,
                        accentColor = retroStatusColors(JobStatus.REJECTED).ink,
                        isSelected = selectedStatus == JobStatus.REJECTED && !isDraftSelected,
                        onClick = {
                            onStatusSelected(if (selectedStatus == JobStatus.REJECTED && !isDraftSelected) null else JobStatus.REJECTED)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactStatusSummaryBar(
    summary: StatusSummary,
    selectedStatus: JobStatus?,
    isDraftSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    val draft = retroDraftColors()
    val applied = retroStatusColors(JobStatus.APPLIED)
    val interviewing = retroStatusColors(JobStatus.INTERVIEWING)
    val offer = retroStatusColors(JobStatus.OFFER)
    val rejected = retroStatusColors(JobStatus.REJECTED)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compact_status_summary_bar")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        val totalSegments = (summary.applied + summary.interviewing + summary.offer + summary.rejected + summary.drafts).coerceAtLeast(0)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(colors.surfaceVariant, RoundedCornerShape(2.dp))
                .border(2.dp, colors.outline, RoundedCornerShape(2.dp))
                .padding(1.dp)
                .clip(RoundedCornerShape(1.dp))
        ) {
            if (totalSegments > 0) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (summary.drafts > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.drafts.toFloat())
                                .fillMaxHeight()
                                .background(draft.ink)
                        )
                    }
                    if (summary.applied > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.applied.toFloat())
                                .fillMaxHeight()
                                .background(applied.ink)
                        )
                    }
                    if (summary.interviewing > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.interviewing.toFloat())
                                .fillMaxHeight()
                                .background(interviewing.ink)
                        )
                    }
                    if (summary.offer > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.offer.toFloat())
                                .fillMaxHeight()
                                .background(offer.ink)
                        )
                    }
                    if (summary.rejected > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.rejected.toFloat())
                                .fillMaxHeight()
                                .background(rejected.ink)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(9.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (summary.drafts > 0) {
                    CompactStatusItem(color = draft.ink, label = "${summary.drafts} ${strings.statusDraft}")
                }
                CompactStatusItem(color = applied.ink, label = "${summary.applied} ${strings.statusApplied}")
                CompactStatusItem(color = interviewing.ink, label = "${summary.interviewing} ${strings.statusInterviewing}")
                CompactStatusItem(color = offer.ink, label = "${summary.offer} ${strings.statusOffer}")
                if (summary.rejected > 0) {
                    CompactStatusItem(color = rejected.ink, label = "${summary.rejected} ${strings.statusRejected}")
                }
            }

            if (isDraftSelected) {
                RetroStamp(text = strings.draftActive, color = draft.ink)
            } else if (selectedStatus != null) {
                RetroStamp(
                    text = strings.selectedStatusIndicator(strings.getStatusDisplayName(selectedStatus)),
                    color = retroStatusColors(selectedStatus).ink
                )
            }
        }
    }
}

@Composable
private fun CompactStatusItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SummaryMetricPill(
    label: String,
    count: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(2.dp)
    val background = if (isSelected) accentColor else colors.surface
    val contentColor = if (isSelected) onAccent(accentColor) else colors.onSurface
    val borderColor = if (isSelected) colors.outline else colors.outlineVariant
    val shadowModifier = if (isSelected) {
        Modifier.hardShadow(2.dp, colors.outline.copy(alpha = 0.45f), shape)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .then(shadowModifier)
            .clip(shape)
            .background(background)
            .border(1.5.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!isSelected) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(accentColor)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )

        Box(
            modifier = Modifier
                .background(
                    if (isSelected) contentColor.copy(alpha = 0.2f)
                    else colors.surfaceVariant,
                    RoundedCornerShape(1.dp)
                )
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
