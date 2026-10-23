package com.example.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroButton
import com.example.presentation.components.RetroButtonStyle
import com.example.presentation.components.hardShadow

@Composable
fun EmptyState(
    isSearching: Boolean,
    onAddApplication: () -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular print seal
        Box(
            modifier = Modifier
                .size(112.dp)
                .rotate(-4f)
                .hardShadow(4.dp, colors.outline.copy(alpha = 0.4f), CircleShape)
                .clip(CircleShape)
                .background(colors.primaryContainer)
                .border(2.dp, colors.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .border(1.dp, colors.primary.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.WorkOutline,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(colors.primary)
            )
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 2.dp)
                    .background(colors.primary)
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(colors.primary)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isSearching) strings.emptySearchTitle else strings.emptyNoAppsTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSearching) strings.emptySearchSubtitle else strings.emptyNoAppsSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isSearching) {
            RetroButton(
                text = strings.clearFilterButton,
                onClick = onClearSearch,
                style = RetroButtonStyle.Secondary,
                modifier = Modifier.testTag("clear_filter_button")
            )
        } else {
            RetroButton(
                text = strings.emptyAddAppButton,
                onClick = onAddApplication,
                icon = Icons.Default.Add,
                style = RetroButtonStyle.Primary,
                modifier = Modifier.testTag("empty_add_application_button")
            )
        }
    }
}
