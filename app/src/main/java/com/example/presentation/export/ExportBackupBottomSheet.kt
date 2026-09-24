package com.example.presentation.export

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
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
import com.example.util.JobExportHelper
import com.example.util.LocalAutoBackupManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBackupBottomSheet(
    applications: List<JobApplication>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    appContainer: AppContainer? = null,
    modifier: Modifier = Modifier
) {
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
                    Toast.makeText(context, "Successfully restored $restoredCount applications!", Toast.LENGTH_LONG).show()
                    onDismiss()
                } else {
                    Toast.makeText(context, "No valid job applications found in file", Toast.LENGTH_SHORT).show()
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
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Data Backup & Local Auto-Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${applications.size} applications currently tracked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_export_sheet_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Local Auto-Sync Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Persistent Local Storage Auto-Sync",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "All applications are saved locally to your device storage on every change. If you uninstall and reinstall, your previous data automatically restores.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "Status: $syncInfo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Restore / Sync Actions
            if (appContainer != null) {
                Text(
                    text = "RESTORE & SYNC",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Option: Scan & Restore Local Backup
                ExportOptionCard(
                    title = "Scan & Auto-Restore Previous Data",
                    subtitle = "Recover all previous job applications from local persistent device backup files.",
                    icon = Icons.Default.Refresh,
                    actionText = "Restore",
                    testTag = "auto_restore_card",
                    onClick = {
                        scope.launch {
                            val restored = LocalAutoBackupManager.autoRestoreIfEmpty(
                                context = context,
                                repository = appContainer.jobRepository
                            )
                            if (restored > 0) {
                                Toast.makeText(context, "🎉 Restored $restored previous applications!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                val loaded = LocalAutoBackupManager.loadAutoBackup(context)
                                if (loaded != null && loaded.isNotEmpty()) {
                                    appContainer.jobRepository.insertApplications(loaded)
                                    Toast.makeText(context, "🎉 Restored ${loaded.size} applications from local storage!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "No previous backup found or data already up to date", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )

                // Option: Import JSON File
                ExportOptionCard(
                    title = "Import from Backup File (JSON)",
                    subtitle = "Select any previously exported job_tracker_backup.json from Files/Drive.",
                    icon = Icons.Default.FileUpload,
                    actionText = "Browse",
                    testTag = "import_json_card",
                    onClick = {
                        filePickerLauncher.launch(arrayOf("application/json", "text/*"))
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Text(
                text = "EXPORT & SHARE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Option 1: CSV Export
            ExportOptionCard(
                title = "Export to CSV (Spreadsheet)",
                subtitle = "Formatted table compatible with Google Sheets, Excel, and Notion database import.",
                icon = Icons.Default.TableChart,
                actionText = "Share CSV",
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
                title = "Export JSON Backup",
                subtitle = "Complete structured raw backup including interview rounds, recruiter contacts, and notes.",
                icon = Icons.Default.DataObject,
                actionText = "Share JSON",
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
                title = "Copy Text Summary",
                subtitle = "Copy a clean, readable Markdown report of your pipeline and interviews to clipboard.",
                icon = Icons.Default.ContentCopy,
                actionText = "Copy to Clipboard",
                testTag = "export_clipboard_card",
                onClick = {
                    val summary = JobExportHelper.generateSummaryMarkdown(applications)
                    val success = JobExportHelper.copyToClipboard(context, summary, "Job Applications Summary")
                    if (success) {
                        Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = actionText,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

