package id.my.f1zz.whenyah.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.my.f1zz.whenyah.data.notification.NotificationService
import id.my.f1zz.whenyah.data.notification.ReminderCoordinator
import id.my.f1zz.whenyah.data.settings.SettingsRepository
import id.my.f1zz.whenyah.data.update.ManualUpdateState
import id.my.f1zz.whenyah.data.update.UpdateManager
import id.my.f1zz.whenyah.data.update.UpdatePreferences
import id.my.f1zz.whenyah.domain.i18n.AppLanguage
import id.my.f1zz.whenyah.domain.i18n.LanguageStore
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.AppSettings
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val notificationService: NotificationService,
    private val languageStore: LanguageStore,
    private val updatePreferences: UpdatePreferences,
    private val updateManager: UpdateManager,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    private val exactAlarmsAllowedState = MutableStateFlow(canScheduleExactAlarms())
    val exactAlarmsAllowed: StateFlow<Boolean> = exactAlarmsAllowedState.asStateFlow()

    val updateChecksEnabled: StateFlow<Boolean> = updatePreferences.updateChecksEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val manualUpdateState: StateFlow<ManualUpdateState> = updateManager.manualState

    fun refreshExactAlarmPermission() {
        val allowed = canScheduleExactAlarms()
        val changed = allowed != exactAlarmsAllowedState.value
        exactAlarmsAllowedState.value = allowed
        if (allowed && changed) {
            viewModelScope.launch { reminderCoordinator.syncAllSchedules() }
        }
    }

    fun openExactAlarmSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData("package:${context.packageName}".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    fun setLanguage(value: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguage(value)
            languageStore.update(value)
            notificationService.ensureChannels()
        }
    }

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(value) }
    }

    fun setAccentColor(value: AccentColor) {
        viewModelScope.launch { settingsRepository.setAccentColor(value) }
    }

    fun setUseDynamicColor(value: Boolean) {
        viewModelScope.launch { settingsRepository.setUseDynamicColor(value) }
    }

    fun setNotificationsEnabled(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(value)
            if (value) {
                reminderCoordinator.syncAllSchedules()
            } else {
                reminderCoordinator.cancelAllSchedules()
            }
        }
    }

    fun setGoalNotificationsEnabled(value: Boolean) {
        viewModelScope.launch { settingsRepository.setGoalNotificationsEnabled(value) }
    }

    fun setSummaryCadence(value: SummaryCadence) {
        viewModelScope.launch {
            settingsRepository.setSummaryCadence(value)
            reminderCoordinator.syncSummarySchedule()
        }
    }

    fun setSummaryTime(value: LocalTime) {
        viewModelScope.launch {
            settingsRepository.setSummaryTime(value)
            reminderCoordinator.syncSummarySchedule()
        }
    }

    fun setUpdateChecksEnabled(value: Boolean) {
        viewModelScope.launch { updatePreferences.setUpdateChecksEnabled(value) }
    }

    fun checkForUpdates() {
        viewModelScope.launch { updateManager.checkManually() }
    }

    private fun canScheduleExactAlarms(): Boolean =
        context.getSystemService(AlarmManager::class.java)
            ?.canScheduleExactAlarms() == true
}
