package com.example.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.JobApplication
import com.example.localization.AppStrings
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroButton
import com.example.presentation.components.RetroButtonStyle
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroSectionHeader
import com.example.presentation.components.RetroStamp
import com.example.util.JobExportHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun UpcomingInterviewsSection(
    interviews: List<JobApplication>,
    onSelectJob: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (interviews.isEmpty()) return

    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("upcoming_interviews_section"),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroSectionHeader(
                title = strings.upcomingInterviews,
                number = "02",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            RetroStamp(
                text = strings.scheduledCount(interviews.size),
                color = colors.primary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(end = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            interviews.forEach { job ->
                UpcomingInterviewItem(
                    job = job,
                    onClick = { onSelectJob(job.id) },
                    onAddToCalendar = { JobExportHelper.addInterviewToCalendar(context, job) }
                )
            }
        }
    }
}

@Composable
private fun UpcomingInterviewItem(
    job: JobApplication,
    onClick: () -> Unit,
    onAddToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    val relativeTimeTag = calculateRelativeDays(job.interviewDate, strings)
    val isUrgent = relativeTimeTag.contains("Today") || relativeTimeTag.contains("Tomorrow") ||
        relativeTimeTag.contains("আজ") || relativeTimeTag.contains("আগামীকাল")

    RetroPanel(
        modifier = modifier
            .width(266.dp)
            .testTag("upcoming_interview_card_${job.id}"),
        onClick = onClick,
        backgroundColor = colors.surface,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        // Printed ticket stub header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isUrgent) colors.errorContainer else colors.primary)
                .padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = relativeTimeTag,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    letterSpacing = 1.1.sp
                ),
                fontWeight = FontWeight.Bold,
                color = if (isUrgent) colors.onErrorContainer else colors.onPrimary,
                maxLines = 1
            )
            if (job.interviewType.isNotBlank()) {
                Text(
                    text = strings.getInterviewStageName(job.interviewType),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isUrgent) {
                        colors.onErrorContainer.copy(alpha = 0.8f)
                    } else {
                        colors.onPrimary.copy(alpha = 0.85f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp)
        ) {
            Text(
                text = job.companyName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.onSurface
            )
            Text(
                text = job.jobTitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(colors.surfaceVariant, RoundedCornerShape(2.dp))
                        .border(1.dp, colors.outline, RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = colors.primary
                    )
                }
                val dateTimeText = buildString {
                    append(job.interviewDate)
                    if (job.interviewTime.isNotBlank()) {
                        append(" · ${job.interviewTime}")
                    }
                }
                Text(
                    text = dateTimeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (job.reminderSet) {
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                    Text(
                        text = strings.alarmReminderActive(job.reminderMinutesBefore),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            RetroButton(
                text = strings.addToCalendar,
                onClick = onAddToCalendar,
                icon = Icons.Default.CalendarMonth,
                style = RetroButtonStyle.Secondary,
                compact = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("calendar_button_${job.id}")
            )
        }
    }
}

private fun calculateRelativeDays(dateStr: String, strings: AppStrings): String {
    if (dateStr.isBlank()) return strings.relativeScheduled
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr.take(10)) ?: return dateStr
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val diff = date.time - today.time
        val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)

        when {
            days == 0L -> strings.relativeToday
            days == 1L -> strings.relativeTomorrow
            days in 2..6 -> strings.relativeInDays(days)
            days > 6 -> strings.relativeInWeeks((days / 7) + 1)
            days == -1L -> strings.relativeYesterday
            else -> dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}
