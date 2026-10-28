package com.example.presentation.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.analytics.PipelineAnalytics
import com.example.domain.analytics.PipelineInsights
import com.example.domain.model.JobApplication
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroSectionHeader
import com.example.presentation.components.hardShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsBottomSheet(
    applications: List<JobApplication>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val analytics = remember(applications) { PipelineInsights.analyze(applications) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("insights_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .hardShadow(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = strings.insightsTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.insightsSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                RetroIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = strings.closeDesc,
                    onClick = onDismiss,
                    size = 38.dp,
                    shadowOffset = 2.dp,
                    modifier = Modifier.testTag("close_insights_sheet_button")
                )
            }

            if (analytics.isEmpty) {
                Text(
                    text = strings.insightsEmpty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("insights_empty")
                )
            } else {
                InsightsContent(analytics)
            }
        }
    }
}

@Composable
private fun InsightsContent(analytics: PipelineAnalytics) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme

    RetroSectionHeader(title = strings.insightsVelocity, number = "01", modifier = Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile(
            value = analytics.last7Days.toString(),
            label = strings.insightsLast7,
            modifier = Modifier
                .weight(1f)
                .testTag("insights_last7")
        )
        StatTile(
            value = analytics.last30Days.toString(),
            label = strings.insightsLast30,
            modifier = Modifier
                .weight(1f)
                .testTag("insights_last30")
        )
    }

    RetroSectionHeader(title = strings.insightsConversion, number = "02", modifier = Modifier.fillMaxWidth())
    RetroPanel(modifier = Modifier.fillMaxWidth(), shadowOffset = 3.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PercentBar(
                label = strings.insightsInterviewRate,
                percent = analytics.interviewRatePercent,
                modifier = Modifier.testTag("insights_interview_rate")
            )
            PercentBar(
                label = strings.insightsResponseRate,
                percent = analytics.responseRatePercent,
                barColor = colors.tertiary
            )
            PercentBar(
                label = strings.insightsOfferRate,
                percent = analytics.offerRatePercent,
                barColor = colors.secondary
            )
        }
    }

    RetroSectionHeader(title = strings.insightsResponseTime, number = "03", modifier = Modifier.fillMaxWidth())
    RetroPanel(modifier = Modifier.fillMaxWidth(), shadowOffset = 3.dp) {
        val avg = analytics.avgDaysToInterview
        if (avg == null) {
            Text(
                text = strings.insightsNoTimingData,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.testTag("insights_no_timing")
            )
        } else {
            Text(
                text = strings.insightsAvgDaysLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
            Text(
                text = strings.insightsAvgDays(avg),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = colors.primary,
                modifier = Modifier.testTag("insights_avg_days")
            )
            Text(
                text = strings.insightsAvgDaysBasedOn(analytics.avgDaysSampleSize),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }

    RetroSectionHeader(title = strings.insightsSources, number = "04", modifier = Modifier.fillMaxWidth())
    RetroPanel(modifier = Modifier.fillMaxWidth(), shadowOffset = 3.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            analytics.sources.take(5).forEach { stat ->
                PercentBar(
                    label = strings.getReferralSourceName(stat.source),
                    detail = strings.insightsSourceDetail(stat.applications, stat.interviews),
                    percent = stat.interviewRatePercent
                )
            }
        }
    }

    Text(
        text = strings.insightsFootnote,
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
    )
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    RetroPanel(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun PercentBar(
    label: String,
    percent: Int,
    modifier: Modifier = Modifier,
    detail: String? = null,
    barColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val colors = MaterialTheme.colorScheme
    val clamped = percent.coerceIn(0, 100)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                if (detail != null) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$clamped%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = colors.onSurface
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.surfaceVariant)
                .border(1.5.dp, colors.outline, RoundedCornerShape(2.dp))
        ) {
            if (clamped > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clamped / 100f)
                        .height(10.dp)
                        .background(barColor)
                )
            }
        }
    }
}
