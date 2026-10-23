package com.example.presentation.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.JobSortOption
import com.example.localization.LocalAppStrings
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroFieldShape
import com.example.presentation.components.retroTextFieldColors

@Composable
fun DashboardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedSort: JobSortOption,
    onSortSelected: (JobSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .testTag("dashboard_search_input"),
            placeholder = {
                Text(
                    text = strings.searchPlaceholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = strings.clearSearch,
                            tint = colors.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RetroFieldShape,
            colors = retroTextFieldColors()
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box {
            RetroIconButton(
                icon = Icons.AutoMirrored.Filled.Sort,
                contentDescription = strings.sortButtonDesc,
                onClick = { sortMenuExpanded = true },
                size = 54.dp,
                shadowOffset = 3.dp,
                tint = colors.primary,
                backgroundColor = colors.surface,
                modifier = Modifier.testTag("sort_button")
            )

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
                shape = RoundedCornerShape(3.dp),
                containerColor = colors.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(2.dp, colors.outline)
            ) {
                JobSortOption.entries.forEach { option ->
                    val isSelected = option == selectedSort
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = strings.getSortDisplayName(option),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.primary else colors.onSurface
                            )
                        },
                        onClick = {
                            onSortSelected(option)
                            sortMenuExpanded = false
                        },
                        modifier = Modifier.testTag("sort_option_${option.name}")
                    )
                }
            }
        }
    }
}
