package com.example.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.OfferBenefit
import com.example.domain.model.WorkModel
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroFieldShape
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.retroTextFieldColors

@Composable
private fun SectionPanel(
    title: String,
    icon: ImageVector,
    testTag: String,
    content: @Composable () -> Unit
) {
    RetroPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        backgroundColor = MaterialTheme.colorScheme.surface,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp, letterSpacing = 1.1.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfferDetailsSection(
    state: AddEditJobUiState,
    onEvent: (AddEditJobUiEvent) -> Unit
) {
    val strings = LocalAppStrings.current
    val selectedBenefits = state.offerBenefits.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    SectionPanel(
        title = strings.offerDetailsSection,
        icon = Icons.Default.EmojiEvents,
        testTag = "offer_details_card"
    ) {
        FieldLabel(strings.workModelLabel)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WorkModel.entries.forEach { model ->
                DateQuickChip(
                    label = strings.getWorkModelName(model.key),
                    isSelected = state.workModel == model.key,
                    onClick = {
                        onEvent(AddEditJobUiEvent.OnWorkModelChange(if (state.workModel == model.key) "" else model.key))
                    }
                )
            }
        }

        OutlinedTextField(
            value = state.offerTotalComp,
            onValueChange = { onEvent(AddEditJobUiEvent.OnOfferTotalCompChange(it)) },
            label = { Text(strings.offerTotalCompLabel) },
            placeholder = { Text(strings.offerTotalCompPlaceholder) },
            leadingIcon = {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            singleLine = true,
            shape = RetroFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offer_total_comp_input"),
            colors = retroTextFieldColors()
        )

        FieldLabel(strings.offerBenefitsLabel)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OfferBenefit.entries.forEach { benefit ->
                DateQuickChip(
                    label = strings.getBenefitName(benefit.key),
                    isSelected = benefit.key in selectedBenefits,
                    onClick = { onEvent(AddEditJobUiEvent.OnOfferBenefitToggle(benefit.key)) },
                    modifier = Modifier.testTag("benefit_chip_${benefit.name}")
                )
            }
        }

        OutlinedTextField(
            value = state.offerPros,
            onValueChange = { onEvent(AddEditJobUiEvent.OnOfferProsChange(it)) },
            label = { Text(strings.offerProsLabel) },
            placeholder = { Text(strings.offerProsPlaceholder) },
            minLines = 2,
            maxLines = 6,
            shape = RetroFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offer_pros_input"),
            colors = retroTextFieldColors()
        )

        OutlinedTextField(
            value = state.offerCons,
            onValueChange = { onEvent(AddEditJobUiEvent.OnOfferConsChange(it)) },
            label = { Text(strings.offerConsLabel) },
            placeholder = { Text(strings.offerConsPlaceholder) },
            minLines = 2,
            maxLines = 6,
            shape = RetroFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offer_cons_input"),
            colors = retroTextFieldColors()
        )
    }
}

@Composable
fun InterviewPrepSection(
    state: AddEditJobUiState,
    onEvent: (AddEditJobUiEvent) -> Unit
) {
    val strings = LocalAppStrings.current

    SectionPanel(
        title = strings.prepSection,
        icon = Icons.Default.Psychology,
        testTag = "interview_prep_card"
    ) {
        OutlinedTextField(
            value = state.prepQuestions,
            onValueChange = { onEvent(AddEditJobUiEvent.OnPrepQuestionsChange(it)) },
            label = { Text(strings.prepQuestionsLabel) },
            placeholder = { Text(strings.prepQuestionsPlaceholder) },
            minLines = 3,
            maxLines = 8,
            shape = RetroFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("prep_questions_input"),
            colors = retroTextFieldColors()
        )

        OutlinedTextField(
            value = state.prepStar,
            onValueChange = { onEvent(AddEditJobUiEvent.OnPrepStarChange(it)) },
            label = { Text(strings.prepStarLabel) },
            placeholder = { Text(strings.prepStarPlaceholder) },
            minLines = 4,
            maxLines = 10,
            shape = RetroFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("prep_star_input"),
            colors = retroTextFieldColors()
        )
        Row {
            DateQuickChip(
                label = strings.prepStarTemplateChip,
                isSelected = false,
                onClick = {
                    val separator = if (state.prepStar.isBlank()) "" else "\n\n"
                    onEvent(AddEditJobUiEvent.OnPrepStarChange(state.prepStar.trimEnd() + separator + strings.prepStarTemplate))
                },
                modifier = Modifier.testTag("prep_star_template_chip")
            )
        }

        OutlinedTextField(
            value = state.prepNegotiation,
            onValueChange = { onEvent(AddEditJobUiEvent.OnPrepNegotiationChange(it)) },
            label = { Text(strings.prepNegotiationLabel) },
            placeholder = { Text(strings.prepNegotiationPlaceholder) },
            minLines = 3,
            maxLines = 8,
            shape = RetroFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("prep_negotiation_input"),
            colors = retroTextFieldColors()
        )
    }
}
