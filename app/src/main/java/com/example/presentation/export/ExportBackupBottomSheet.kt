package com.example.presentation.export

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SheetState
import kotlinx.coroutines.launch
import com.example.presentation.components.RetroIconButton
import com.example.presentation.components.RetroPanel
import com.example.presentation.components.RetroSectionHeader
import com.example.presentation.components.hardShadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import com.example.domain.model.JobApplication
import com.example.localization.LocalAppStrings
import com.example.util.FeedbackHelper
import com.example.util.JobExportHelper
import com.example.util.LocalAutoBackupManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBackupBottomSheet(
    applications: List<JobApplication>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    appContainer: AppContainer? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncInfo = LocalAutoBackupManager.getLastSyncInfo(context)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && appContainer != null) {
            scope.launch {
                val restoredCount = LocalAutoBackupManager.restoreFromUri(
                    context = context,
                    uri = uri,
                    repository = appContainer.jobRepository
                )
                if (restoredCount > 0) {
                    Toast.makeText(context, strings.toastRestoredCount(restoredCount), Toast.LENGTH_LONG).show()
                    onDismiss()
                } else {
                    Toast.makeText(context, strings.toastNoValidApps, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("export_backup_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
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
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = strings.backupSheetTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.applicationsTrackedCount(applications.size),
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
                    modifier = Modifier.testTag("close_export_sheet_button")
                )
            }

            // Local Auto-Sync Status Banner
            RetroPanel(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                borderColor = MaterialTheme.colorScheme.primary,
                shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.persistentSyncTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = strings.persistentSyncDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = strings.backupStatusLabel(syncInfo),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline
            )

            // Restore / Sync Actions
            if (appContainer != null) {
                RetroSectionHeader(
                    title = strings.restoreAndSyncSection,
                    number = "01",
                    modifier = Modifier.fillMaxWidth()
                )

                // Option: Scan & Restore Local Backup
                ExportOptionCard(
                    title = strings.scanRestoreTitle,
                    subtitle = strings.scanRestoreSubtitle,
                    icon = Icons.Default.Refresh,
                    actionText = strings.actionRestore,
                    testTag = "auto_restore_card",
                    onClick = {
                        scope.launch {
                            val restored = LocalAutoBackupManager.autoRestoreIfEmpty(
                                context = context,
                                repository = appContainer.jobRepository
                            )
                            if (restored > 0) {
                                Toast.makeText(context, strings.toastRestoredPrevious(restored), Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                val loaded = LocalAutoBackupManager.loadAutoBackup(context)
                                if (loaded != null && loaded.isNotEmpty()) {
                                    appContainer.jobRepository.insertApplications(loaded)
                                    Toast.makeText(context, strings.toastRestoredLocal(loaded.size), Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, strings.toastNoBackup, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )

                // Option: Import JSON File
                ExportOptionCard(
                    title = strings.importJsonTitle,
                    subtitle = strings.importJsonSubtitle,
                    icon = Icons.Default.FileUpload,
                    actionText = strings.actionBrowse,
                    testTag = "import_json_card",
                    onClick = {
                        filePickerLauncher.launch(arrayOf("application/json", "text/*"))
                    }
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            RetroSectionHeader(
                title = strings.exportShareSection,
                number = "02",
                modifier = Modifier.fillMaxWidth()
            )

            // Option 1: CSV Export
            ExportOptionCard(
                title = strings.exportCsvTitle,
                subtitle = strings.exportCsvSubtitle,
                icon = Icons.Default.TableChart,
                actionText = strings.actionShareCsv,
                testTag = "export_csv_card",
                onClick = {
                    val csv = JobExportHelper.generateCsv(applications)
                    JobExportHelper.shareContent(
                        context = context,
                        content = csv,
                        title = "Job_Applications_Export.csv",
                        mimeType = "text/csv"
                    )
                }
            )

            // Option 2: JSON Backup
            ExportOptionCard(
                title = strings.exportJsonTitle,
                subtitle = strings.exportJsonSubtitle,
                icon = Icons.Default.DataObject,
                actionText = strings.actionShareJson,
                testTag = "export_json_card",
                onClick = {
                    val json = JobExportHelper.generateJson(applications)
                    JobExportHelper.shareContent(
                        context = context,
                        content = json,
                        title = "job_tracker_backup.json",
                        mimeType = "application/json"
                    )
                }
            )

            // Option 3: Copy Markdown / Text Summary
            ExportOptionCard(
                title = strings.copySummaryTitle,
                subtitle = strings.copySummarySubtitle,
                icon = Icons.Default.ContentCopy,
                actionText = strings.copyToClipboardAction,
                testTag = "export_clipboard_card",
                onClick = {
                    val summary = JobExportHelper.generateSummaryMarkdown(applications)
                    val success = JobExportHelper.copyToClipboard(context, summary, "Job Applications Summary")
                    if (success) {
                        Toast.makeText(context, strings.toastSummaryCopied, Toast.LENGTH_SHORT).show()
                    }
                }
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline
            )

            RetroSectionHeader(
                title = strings.feedbackSection,
                number = "03",
                modifier = Modifier.fillMaxWidth()
            )

            // Option 4: Tester feedback by email, with device details attached
            ExportOptionCard(
                title = strings.feedbackTitle,
                subtitle = strings.feedbackSubtitle,
                icon = Icons.Default.Feedback,
                actionText = strings.feedbackTitle,
                testTag = "send_feedback_card",
                onClick = {
                    if (!FeedbackHelper.send(context)) {
                        Toast.makeText(context, strings.feedbackNoEmailApp, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(3.dp)

    RetroPanel(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        onClick = onClick,
        backgroundColor = colors.surface,
        shadowOffset = 3.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .hardShadow(2.dp, colors.outline.copy(alpha = 0.4f), shape)
                    .clip(shape)
                    .background(colors.primaryContainer)
                    .border(2.dp, colors.outline, shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = actionText,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

