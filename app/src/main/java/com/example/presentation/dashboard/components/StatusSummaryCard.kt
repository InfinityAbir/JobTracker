package com.example.presentation.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.JobStatus
import com.example.domain.model.StatusSummary
import com.example.ui.theme.StatusAppliedLight
import com.example.ui.theme.StatusDraftLight
import com.example.ui.theme.StatusInterviewingLight
import com.example.ui.theme.StatusOfferLight
import com.example.ui.theme.StatusRejectedLight

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
    var internalExpanded by remember { mutableStateOf(false) }
    val expanded = isExpanded ?: internalExpanded
    val toggleExpanded = onToggleExpanded ?: { internalExpanded = !internalExpanded }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Tappable Header Row to toggle dropdown expansion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = toggleExpanded)
                    .testTag("pipeline_card_header")
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Application Pipeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${summary.total} Total",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val rotationState by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = tween(durationMillis = 250),
                        label = "expand_arrow_rotation"
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse Application Pipeline" else "Expand Application Pipeline",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotationState)
                            .testTag("pipeline_expand_button")
                    )
                }
            }

            // Compact status summary bar (shown when collapsed)
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

            // Full status filter chips (revealed on expansion via AnimatedVisibility)
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
                        // All Chip
                        SummaryMetricPill(
                            label = "All",
                            count = summary.total,
                            accentColor = MaterialTheme.colorScheme.primary,
                            isSelected = selectedStatus == null && !isDraftSelected,
                            onClick = { onStatusSelected(null) }
                        )

                        // Draft Chip
                        SummaryMetricPill(
                            label = "Draft",
                            count = summary.drafts,
                            accentColor = StatusDraftLight,
                            isSelected = isDraftSelected,
                            onClick = onDraftSelected
                        )

                        // Applied Chip
                        SummaryMetricPill(
                            label = "Applied",
                            count = summary.applied,
                            accentColor = StatusAppliedLight,
                            isSelected = selectedStatus == JobStatus.APPLIED && !isDraftSelected,
                            onClick = {
                                onStatusSelected(if (selectedStatus == JobStatus.APPLIED && !isDraftSelected) null else JobStatus.APPLIED)
                            }
                        )

                        // Interviewing Chip
                        SummaryMetricPill(
                            label = "Interviewing",
                            count = summary.interviewing,
                            accentColor = StatusInterviewingLight,
                            isSelected = selectedStatus == JobStatus.INTERVIEWING && !isDraftSelected,
                            onClick = {
                                onStatusSelected(if (selectedStatus == JobStatus.INTERVIEWING && !isDraftSelected) null else JobStatus.INTERVIEWING)
                            }
                        )

                        // Offer Chip
                        SummaryMetricPill(
                            label = "Offer",
                            count = summary.offer,
                            accentColor = StatusOfferLight,
                            isSelected = selectedStatus == JobStatus.OFFER && !isDraftSelected,
                            onClick = {
                                onStatusSelected(if (selectedStatus == JobStatus.OFFER && !isDraftSelected) null else JobStatus.OFFER)
                            }
                        )

                        // Rejected Chip
                        SummaryMetricPill(
                            label = "Rejected",
                            count = summary.rejected,
                            accentColor = StatusRejectedLight,
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
}

@Composable
private fun CompactStatusSummaryBar(
    summary: StatusSummary,
    selectedStatus: JobStatus?,
    isDraftSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compact_status_summary_bar")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Segmented multi-color ratio bar showing distribution across stages
        val totalSegments = (summary.applied + summary.interviewing + summary.offer + summary.rejected + summary.drafts).coerceAtLeast(0)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            if (totalSegments > 0) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (summary.drafts > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.drafts.toFloat())
                                .fillMaxHeight()
                                .background(StatusDraftLight)
                        )
                    }
                    if (summary.applied > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.applied.toFloat())
                                .fillMaxHeight()
                                .background(StatusAppliedLight)
                        )
                    }
                    if (summary.interviewing > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.interviewing.toFloat())
                                .fillMaxHeight()
                                .background(StatusInterviewingLight)
                        )
                    }
                    if (summary.offer > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.offer.toFloat())
                                .fillMaxHeight()
                                .background(StatusOfferLight)
                        )
                    }
                    if (summary.rejected > 0) {
                        Box(
                            modifier = Modifier
                                .weight(summary.rejected.toFloat())
                                .fillMaxHeight()
                                .background(StatusRejectedLight)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Compact status micro-dots and summary counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (summary.drafts > 0) {
                    CompactStatusItem(color = StatusDraftLight, label = "${summary.drafts} Draft")
                }
                CompactStatusItem(color = StatusAppliedLight, label = "${summary.applied} Applied")
                CompactStatusItem(color = StatusInterviewingLight, label = "${summary.interviewing} Interview")
                CompactStatusItem(color = StatusOfferLight, label = "${summary.offer} Offer")
                if (summary.rejected > 0) {
                    CompactStatusItem(color = StatusRejectedLight, label = "${summary.rejected} Rejected")
                }
            }

            if (isDraftSelected) {
                Text(
                    text = "• Draft active",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = StatusDraftLight
                )
            } else if (selectedStatus != null) {
                Text(
                    text = "• ${selectedStatus.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
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
    val targetBgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val animatedBgColor by animateColorAsState(targetValue = targetBgColor, animationSpec = tween(200), label = "pillBg")

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderModifier = if (isSelected) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
    } else {
        Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    }

    Row(
        modifier = Modifier
            .then(borderModifier)
            .clip(RoundedCornerShape(14.dp))
            .background(animatedBgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
