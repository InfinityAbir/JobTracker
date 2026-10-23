package com.example.presentation.dashboard.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroDashedDivider
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroTag
import com.example.presentation.components.hardShadow
import com.example.presentation.components.retroStatusColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobCard(
    application: JobApplication,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentOnDelete by rememberUpdatedState(onDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                currentOnDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val shape = RoundedCornerShape(4.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 3.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .border(2.dp, MaterialTheme.colorScheme.error, shape)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.deleteSwipeAction,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.deleteSwipeAction,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        content = {
            JobCardContent(
                application = application,
                onClick = onClick,
                modifier = modifier
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JobCardContent(
    application: JobApplication,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    val monogramShape = RoundedCornerShape(3.dp)

    RetroPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .testTag("job_card_${application.id}"),
        onClick = onClick,
        backgroundColor = colors.surface,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Company monogram stamp
            val initial = application.companyName.take(1).uppercase().ifBlank { "?" }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .hardShadow(2.dp, colors.outline.copy(alpha = 0.4f), monogramShape)
                    .clip(monogramShape)
                    .background(colors.primaryContainer)
                    .border(2.dp, colors.outline, monogramShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                    fontWeight = FontWeight.Black,
                    color = colors.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${application.id.toString().padStart(4, '0')}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp
                    ),
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = application.jobTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = application.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            StatusChip(status = application.status)
        }

        Spacer(modifier = Modifier.height(10.dp))
        RetroDashedDivider(color = colors.outlineVariant.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (application.dateApplied.isNotBlank()) {
                RetroTag(
                    text = application.dateApplied,
                    icon = Icons.Default.CalendarToday,
                    color = colors.onSurfaceVariant
                )
            }

            if (application.salary.isNotBlank()) {
                RetroTag(
                    text = application.salary,
                    icon = Icons.Default.AttachMoney,
                    color = colors.onSecondaryContainer,
                    borderColor = colors.secondary,
                    backgroundColor = colors.secondaryContainer.copy(alpha = 0.55f)
                )
            }

            if (application.recruiterName.isNotBlank() || application.referralSource.isNotBlank()) {
                RetroTag(
                    text = application.recruiterName.ifBlank { application.referralSource },
                    icon = Icons.Default.Person,
                    color = colors.onTertiaryContainer,
                    borderColor = colors.tertiary,
                    backgroundColor = colors.tertiaryContainer.copy(alpha = 0.5f)
                )
            }
        }

        // Scheduled interview ticket
        if (application.interviewDate.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.primaryContainer.copy(alpha = 0.45f))
                    .border(1.5.dp, colors.outline, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(52.dp)
                        .background(colors.primary)
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = colors.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.interviewBadgeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.1.sp
                            ),
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        val timePart = if (application.interviewTime.isNotBlank()) " @ ${application.interviewTime}" else ""
                        val stagePart = if (application.interviewType.isNotBlank()) " · ${strings.getInterviewStageName(application.interviewType)}" else ""
                        Text(
                            text = "${application.interviewDate}$timePart$stagePart",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (application.reminderSet) {
                        Text(
                            text = strings.interviewAlertMinutes(application.reminderMinutesBefore),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (application.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(9.dp))
            Row {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(colors.outlineVariant)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = application.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    status: JobStatus,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = retroStatusColors(status)
    val shape = RoundedCornerShape(2.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(palette.background)
            .border(1.5.dp, palette.ink, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(palette.ink)
        )
        Text(
            text = strings.getStatusDisplayName(status),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                letterSpacing = 0.7.sp
            ),
            color = palette.ink,
            maxLines = 1
        )
    }
}
