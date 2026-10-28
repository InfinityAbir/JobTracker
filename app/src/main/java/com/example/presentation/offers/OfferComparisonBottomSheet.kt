package com.example.presentation.offers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.JobApplication
import com.example.domain.model.OfferBenefit
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.hardShadow

const val MAX_COMPARED_OFFERS = 3

/** Offers newest first, capped at [MAX_COMPARED_OFFERS], honouring the user's picks when they made any. */
internal fun chooseComparedOffers(offers: List<JobApplication>, pickedIds: List<Long>): List<JobApplication> {
    val picked = pickedIds.mapNotNull { id -> offers.firstOrNull { it.id == id } }
    return if (picked.isNotEmpty()) picked.take(MAX_COMPARED_OFFERS) else offers.take(MAX_COMPARED_OFFERS)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferComparisonBottomSheet(
    offers: List<JobApplication>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var pickedIds by rememberSaveable { mutableStateOf<List<Long>>(emptyList()) }
    val compared = chooseComparedOffers(offers, pickedIds)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("offer_comparison_sheet")
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
                            .background(MaterialTheme.colorScheme.secondary)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = strings.compareOffersTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.compareOffersSubtitle,
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
                    modifier = Modifier.testTag("close_offer_comparison_button")
                )
            }

            if (offers.size > MAX_COMPARED_OFFERS) {
                Text(
                    text = strings.compareSelectHint,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    offers.forEach { offer ->
                        val selected = compared.any { it.id == offer.id }
                        PickChip(
                            label = offer.companyName,
                            selected = selected,
                            onClick = {
                                val current = compared.map { it.id }
                                pickedIds = when {
                                    selected && current.size > 1 -> current - offer.id
                                    !selected && current.size < MAX_COMPARED_OFFERS -> current + offer.id
                                    else -> current
                                }
                            },
                            modifier = Modifier.testTag("offer_pick_${offer.id}")
                        )
                    }
                }
            }

            RetroPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offer_comparison_table"),
                shadowOffset = 3.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Columns(compared) { offer ->
                        Text(
                            text = offer.companyName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = offer.jobTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    CompareRow(strings.compareRowBaseSalary, compared) { it.salary.valueOrDash() }
                    CompareRow(strings.compareRowTotalComp, compared) { it.offerTotalComp.valueOrDash() }
                    CompareRow(strings.compareRowWorkModel, compared) {
                        if (it.workModel.isBlank()) "—" else strings.getWorkModelName(it.workModel)
                    }

                    RowLabel(strings.compareRowBenefits)
                    OfferBenefit.entries.forEach { benefit ->
                        Text(
                            text = strings.getBenefitName(benefit.key),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Columns(compared) { offer ->
                            val included = benefit.key in offer.benefitKeys
                            Icon(
                                imageVector = if (included) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (included) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag("benefit_${benefit.name}_${offer.id}_${if (included) "yes" else "no"}")
                            )
                        }
                    }

                    CompareLines(strings.compareRowPros, compared, { it.offerPros })
                    CompareLines(strings.compareRowCons, compared, { it.offerCons })
                }
            }
        }
    }
}

private fun String.valueOrDash(): String = ifBlank { "—" }

@Composable
private fun RowLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

/** One equal-width column per offer. */
@Composable
private fun Columns(offers: List<JobApplication>, cell: @Composable androidx.compose.foundation.layout.ColumnScope.(JobApplication) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        offers.forEach { offer ->
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                cell(offer)
            }
        }
        // Keep columns the same width whether 2 or 3 offers are shown.
        repeat(MAX_COMPARED_OFFERS - offers.size) { Spacer(modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun CompareRow(label: String, offers: List<JobApplication>, value: (JobApplication) -> String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RowLabel(label)
        Columns(offers) { offer ->
            Text(
                text = value(offer),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CompareLines(label: String, offers: List<JobApplication>, text: (JobApplication) -> String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RowLabel(label)
        Columns(offers) { offer ->
            val lines = text(offer).lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) {
                Text("—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                lines.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PickChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.primaryContainer else colors.surface)
            .border(1.5.dp, if (selected) colors.outline else colors.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            maxLines = 1
        )
    }
}
