package id.my.f1zz.whenyah.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.notification.ReminderCoordinator
import id.my.f1zz.whenyah.data.settings.SettingsRepository
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.AppSettings
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderCoordinator: ReminderCoordinator,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

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
                reminderCoordinator.syncAllSchedules(force = true)
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
            reminderCoordinator.syncSummarySchedule(force = true)
        }
    }

    fun setSummaryTime(value: LocalTime) {
        viewModelScope.launch {
            settingsRepository.setSummaryTime(value)
            reminderCoordinator.syncSummarySchedule(force = true)
        }
    }
}
