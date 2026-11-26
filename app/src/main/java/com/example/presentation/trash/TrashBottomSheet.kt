package com.example.presentation.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.JobApplication
import com.example.localization.LocalAppStrings
import com.example.presentation.dashboard.components.StatusChip
import com.example.presentation.components.RetroButton
import com.example.presentation.components.RetroButtonStyle
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.hardShadow
import java.util.concurrent.TimeUnit

/**
 * The trash: every application the user deleted, newest deletion first. Items can be restored one
 * by one, deleted permanently one by one, or all at once with "Empty trash". Permanent deletion
 * always asks for confirmation because it cannot be undone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBottomSheet(
    trashed: List<JobApplication>,
    sheetState: SheetState,
    onRestore: (JobApplication) -> Unit,
    onDeleteForever: (JobApplication) -> Unit,
    onEmptyTrash: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    nowMillis: Long = System.currentTimeMillis()
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme
    var pendingDelete by remember { mutableStateOf<JobApplication?>(null) }
    var showEmptyDialog by remember { mutableStateOf(false) }

    pendingDelete?.let { job ->
        ConfirmDialog(
            title = strings.deleteForeverDialogTitle,
            message = strings.deleteForeverDialogMessage(job.companyName),
            confirmText = strings.deleteForeverButton,
            onConfirm = {
                pendingDelete = null
                onDeleteForever(job)
            },
            onCancel = { pendingDelete = null },
            tagPrefix = "delete_forever"
        )
    }

    if (showEmptyDialog) {
        ConfirmDialog(
            title = strings.emptyTrashDialogTitle,
            message = strings.emptyTrashDialogMessage(trashed.size),
            confirmText = strings.emptyTrashButton,
            onConfirm = {
                showEmptyDialog = false
                onEmptyTrash()
            },
            onCancel = { showEmptyDialog = false },
            tagPrefix = "empty_trash"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        modifier = modifier.testTag("trash_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
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
                                .hardShadow(2.dp, colors.outline.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.primary)
                                .border(2.dp, colors.outline, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.trashTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = strings.trashSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    RetroIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = strings.closeTrashDesc,
                        onClick = onDismiss,
                        size = 38.dp,
                        shadowOffset = 2.dp,
                        modifier = Modifier.testTag("close_trash_sheet_button")
                    )
                }
            }

            if (trashed.isEmpty()) {
                item(key = "empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("trash_empty_state"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = colors.outlineVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = strings.trashEmpty,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface
                        )
                        Text(
                            text = strings.trashEmptyHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            } else {
                item(key = "empty_all") {
                    RetroButton(
                        text = "${strings.emptyTrashButton} (${trashed.size})",
                        onClick = { showEmptyDialog = true },
                        icon = Icons.Default.DeleteForever,
                        style = RetroButtonStyle.Danger,
                        compact = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_trash_button")
                    )
                }

                items(items = trashed, key = { it.id }) { job ->
                    TrashItem(
                        job = job,
                        daysAgo = daysSince(job.deletedAt, nowMillis),
                        onRestore = { onRestore(job) },
                        onDeleteForever = { pendingDelete = job }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrashItem(
    job: JobApplication,
    daysAgo: Int,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = MaterialTheme.colorScheme

    RetroPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trash_item_${job.id}"),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.jobTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = job.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusChip(status = job.status, modifier = Modifier.padding(start = 8.dp))
        }

        Text(
            text = strings.deletedAgo(daysAgo),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RetroButton(
                text = strings.restoreButton,
                onClick = onRestore,
                icon = Icons.Default.Restore,
                style = RetroButtonStyle.Secondary,
                compact = true,
                modifier = Modifier
                    .weight(0.85f)
                    .testTag("trash_restore_button_${job.id}")
            )
            RetroButton(
                text = strings.deleteForeverButton,
                onClick = onDeleteForever,
                icon = Icons.Default.DeleteForever,
                style = RetroButtonStyle.Danger,
                compact = true,
                // "Delete forever" is the longer label: it gets the larger share so it never truncates.
                modifier = Modifier
                    .weight(1.15f)
                    .testTag("trash_delete_forever_button_${job.id}")
            )
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    tagPrefix: String
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            RetroButton(
                text = confirmText,
                onClick = onConfirm,
                style = RetroButtonStyle.Danger,
                compact = true,
                modifier = Modifier.testTag("${tagPrefix}_confirm_button")
            )
        },
        dismissButton = {
            RetroButton(
                text = strings.cancelButton,
                onClick = onCancel,
                style = RetroButtonStyle.Secondary,
                compact = true,
                modifier = Modifier.testTag("${tagPrefix}_cancel_button")
            )
        },
        modifier = Modifier.testTag("${tagPrefix}_dialog")
    )
}

internal fun daysSince(deletedAt: Long, nowMillis: Long): Int {
    if (deletedAt <= 0L) return 0
    return TimeUnit.MILLISECONDS.toDays((nowMillis - deletedAt).coerceAtLeast(0L)).toInt()
}
