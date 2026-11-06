package id.my.f1zz.whenyah.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.AppSettings
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { failure ->
            if (failure is IOException) {
                emit(emptyPreferences())
            } else {
                throw failure
            }
        }
        .map { preferences ->
            AppSettings(
                themeMode = preferences[Keys.THEME_MODE]
                    ?.let { value -> runCatching { ThemeMode.valueOf(value) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                accentColor = preferences[Keys.ACCENT_COLOR]
                    ?.let { value -> runCatching { AccentColor.valueOf(value) }.getOrNull() }
                    ?: AccentColor.DEFAULT,
                useDynamicColor = preferences[Keys.USE_DYNAMIC_COLOR] ?: true,
                notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: false,
                goalNotificationsEnabled = preferences[Keys.GOAL_NOTIFICATIONS_ENABLED] ?: true,
                summaryCadence = preferences[Keys.SUMMARY_CADENCE]
                    ?.let { value -> runCatching { SummaryCadence.valueOf(value) }.getOrNull() }
                    ?: SummaryCadence.OFF,
                summaryTime = preferences[Keys.SUMMARY_TIME]
                    ?.let { value -> runCatching { LocalTime.parse(value) }.getOrNull() }
                    ?: LocalTime.of(9, 0),
            )
        }

    suspend fun currentSettings(): AppSettings = settings.first()

    suspend fun replaceSettings(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = settings.themeMode.name
            preferences[Keys.ACCENT_COLOR] = settings.accentColor.name
            preferences[Keys.USE_DYNAMIC_COLOR] = settings.useDynamicColor
            preferences[Keys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[Keys.GOAL_NOTIFICATIONS_ENABLED] = settings.goalNotificationsEnabled
            preferences[Keys.SUMMARY_CADENCE] = settings.summaryCadence.name
            preferences[Keys.SUMMARY_TIME] = settings.summaryTime.toString()
        }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = value.name }
    }

    suspend fun setAccentColor(value: AccentColor) {
        context.settingsDataStore.edit { it[Keys.ACCENT_COLOR] = value.name }
    }

    suspend fun setUseDynamicColor(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.USE_DYNAMIC_COLOR] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    }

    suspend fun setGoalNotificationsEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.GOAL_NOTIFICATIONS_ENABLED] = value }
    }

    suspend fun setSummaryCadence(value: SummaryCadence) {
        context.settingsDataStore.edit { it[Keys.SUMMARY_CADENCE] = value.name }
    }

    suspend fun setSummaryTime(value: LocalTime) {
        context.settingsDataStore.edit { it[Keys.SUMMARY_TIME] = value.toString() }
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val GOAL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("goal_notifications_enabled")
        val SUMMARY_CADENCE = stringPreferencesKey("summary_cadence")
        val SUMMARY_TIME = stringPreferencesKey("summary_time")
    }
}
