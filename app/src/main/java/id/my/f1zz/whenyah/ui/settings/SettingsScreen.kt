package id.my.f1zz.whenyah.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import id.my.f1zz.whenyah.ui.util.formatTime
import java.time.LocalTime

@Composable
fun SettingsScreen(
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var permissionDenied by rememberSaveable { mutableStateOf(false) }

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
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsCard(title = "Appearance") {
                Text(
                    text = "Theme",
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
                                        ThemeMode.SYSTEM -> "System"
                                        ThemeMode.LIGHT -> "Light"
                                        ThemeMode.DARK -> "Dark"
                                    }
                                )
                            },
                        )
                    }
                }

                SettingSwitchRow(
                    title = "Dynamic color",
                    supportingText = "Use the device wallpaper palette when available.",
                    checked = settings.useDynamicColor,
                    onCheckedChange = viewModel::setUseDynamicColor,
                )

                Text(
                    text = "Accent color",
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
                                        AccentColor.DEFAULT -> "Default"
                                        AccentColor.BLUE -> "Blue"
                                        AccentColor.GREEN -> "Green"
                                        AccentColor.AMBER -> "Amber"
                                        AccentColor.ROSE -> "Rose"
                                    }
                                )
                            },
                        )
                    }
                }
                if (settings.useDynamicColor) {
                    Text(
                        text = "Turn off dynamic color to use a custom accent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SettingsCard(title = "Notifications") {
                SettingSwitchRow(
                    title = "Enable notifications",
                    supportingText = if (permissionDenied) {
                        "Permission denied. Enable notification permission in system settings."
                    } else {
                        "Reminders and summaries are best-effort and may be delayed by Android."
                    },
                    checked = settings.notificationsEnabled,
                    onCheckedChange = ::onNotificationsToggle,
                )

                SettingSwitchRow(
                    title = "Goal reached",
                    supportingText = "Notify when an actual deposit reaches an item's target.",
                    checked = settings.goalNotificationsEnabled,
                    enabled = settings.notificationsEnabled,
                    onCheckedChange = viewModel::setGoalNotificationsEnabled,
                )

                Text(
                    text = "Contribution reminders are configured per plan in the savings-plan editor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "Progress summary",
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
                                        SummaryCadence.OFF -> "Off"
                                        SummaryCadence.WEEKLY -> "Weekly"
                                        SummaryCadence.MONTHLY -> "Monthly"
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
                        Text("Summary time: ${formatTime(settings.summaryTime)}")
                    }
                }
            }
        }

        item {
            SettingsCard(title = "Data") {
                Button(
                    onClick = onOpenArchive,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Purchased archive")
                }
                Text(
                    text = "Backup and restore are not available yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SettingsCard(title = "About") {
                Text(
                    text = "When Yah 1.0",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Offline-first wishlist and savings tracker • IDR only",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
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
        modifier = Modifier.fillMaxWidth(),
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
