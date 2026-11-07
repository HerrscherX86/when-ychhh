package id.my.f1zz.whenyah.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.domain.i18n.AppLanguage
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.util.formatTime
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    backupViewModel: BackupViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val backupState by backupViewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri != null) backupViewModel.export(uri)
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) pendingRestoreUri = uri
    }
    val backupBusy = backupState.isExporting || backupState.isRestoring

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            permissionDenied = false
            viewModel.setNotificationsEnabled(true)
        } else {
            permissionDenied = true
        }
    }

    fun onNotificationsToggle(enabled: Boolean) {
        if (!enabled) {
            permissionDenied = false
            viewModel.setNotificationsEnabled(false)
            return
        }

        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionDenied = false
            viewModel.setNotificationsEnabled(true)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsSection(icon = Icons.Outlined.Palette, title = strings.appearance) {
                Text(
                    text = strings.theme,
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> strings.system
                                        ThemeMode.LIGHT -> strings.light
                                        ThemeMode.DARK -> strings.dark
                                    }
                                )
                            },
                        )
                    }
                }

                SettingSwitchRow(
                    title = strings.dynamicColor,
                    supportingText = strings.dynamicColorHelp,
                    checked = settings.useDynamicColor,
                    onCheckedChange = viewModel::setUseDynamicColor,
                )

                Text(
                    text = strings.accentColor,
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AccentColor.entries.forEach { accent ->
                        FilterChip(
                            selected = settings.accentColor == accent,
                            onClick = { viewModel.setAccentColor(accent) },
                            enabled = !settings.useDynamicColor,
                            label = {
                                Text(
                                    when (accent) {
                                        AccentColor.DEFAULT -> strings.accentDefault
                                        AccentColor.BLUE -> strings.accentBlue
                                        AccentColor.GREEN -> strings.accentGreen
                                        AccentColor.AMBER -> strings.accentAmber
                                        AccentColor.ROSE -> strings.accentRose
                                    }
                                )
                            },
                        )
                    }
                }
                if (settings.useDynamicColor) {
                    Text(
                        text = strings.accentHelp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SettingsSection(icon = Icons.Outlined.Language, title = strings.language) {
                Text(
                    text = strings.languageHelp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.language == AppLanguage.ENGLISH,
                        onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                        label = { Text(strings.languageEnglish) },
                    )
                    FilterChip(
                        selected = settings.language == AppLanguage.INDONESIAN,
                        onClick = { viewModel.setLanguage(AppLanguage.INDONESIAN) },
                        label = { Text(strings.languageIndonesian) },
                    )
                }
            }
        }

        item {
            SettingsSection(icon = Icons.Outlined.Notifications, title = strings.notifications) {
                SettingSwitchRow(
                    title = strings.enableNotifications,
                    supportingText = if (permissionDenied) {
                        strings.permissionDenied
                    } else {
                        strings.notificationBestEffort
                    },
                    checked = settings.notificationsEnabled,
                    onCheckedChange = ::onNotificationsToggle,
                )

                SettingSwitchRow(
                    title = strings.goalNotifications,
                    supportingText = strings.goalNotificationsHelp,
                    checked = settings.goalNotificationsEnabled,
                    enabled = settings.notificationsEnabled,
                    onCheckedChange = viewModel::setGoalNotificationsEnabled,
                )

                Text(
                    text = strings.contributionReminderHelp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = strings.progressSummary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCadence.entries.forEach { cadence ->
                        FilterChip(
                            selected = settings.summaryCadence == cadence,
                            onClick = { viewModel.setSummaryCadence(cadence) },
                            enabled = settings.notificationsEnabled,
                            label = {
                                Text(
                                    when (cadence) {
                                        SummaryCadence.OFF -> strings.off
                                        SummaryCadence.WEEKLY -> strings.weekly
                                        SummaryCadence.MONTHLY -> strings.monthly
                                    }
                                )
                            },
                        )
                    }
                }

                if (settings.summaryCadence != SummaryCadence.OFF) {
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.setSummaryTime(LocalTime.of(hour, minute))
                                },
                                settings.summaryTime.hour,
                                settings.summaryTime.minute,
                                true,
                            ).show()
                        },
                        enabled = settings.notificationsEnabled,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(strings.summaryTimeLabel(formatTime(settings.summaryTime)))
                    }
                }
            }
        }

        item {
            SettingsSection(icon = Icons.Outlined.Storage, title = strings.data) {
                Button(
                    onClick = onOpenArchive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.purchasedArchive)
                }

                OutlinedButton(
                    onClick = { exportLauncher.launch(backupFileName()) },
                    enabled = !backupBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (backupState.isExporting) strings.exporting else strings.exportBackup)
                }

                OutlinedButton(
                    onClick = {
                        restoreLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/x-zip-compressed",
                                "application/octet-stream",
                            )
                        )
                    },
                    enabled = !backupBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (backupState.isRestoring) strings.restoring else strings.restoreBackup)
                }

                if (backupBusy) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                backupState.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                backupState.error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Text(
                    text = strings.restoreReplacesData,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SettingsSection(icon = Icons.Outlined.Info, title = strings.about) {
                Text(
                    text = strings.appVersion,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = strings.appTagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text(strings.restoreBackupTitle) },
            text = { Text(strings.restoreBackupMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRestoreUri = null
                        backupViewModel.restore(uri)
                    },
                ) {
                    Text(strings.restore)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}

private fun backupFileName(): String {
    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    return "whenyah-backup-$stamp.zip"
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
