package id.my.f1zz.whenyah.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import id.my.f1zz.whenyah.domain.i18n.AppLanguage
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.AppSettings
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val preferencesFlow = context.settingsDataStore.data
        .catch { failure ->
            if (failure is IOException) {
                emit(emptyPreferences())
            } else {
                throw failure
            }
        }

    val settings: Flow<AppSettings> = preferencesFlow
        .map { preferences ->
            AppSettings(
                language = preferences[Keys.LANGUAGE]
                    ?.let { value -> runCatching { AppLanguage.valueOf(value) }.getOrNull() }
                    ?: AppLanguage.ENGLISH,
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
                summaryAnchorDate = preferences[Keys.SUMMARY_ANCHOR_DATE]
                    ?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() },
            )
        }

    val hasCompletedOnboarding: Flow<Boolean> = preferencesFlow
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETED] ?: false }

    suspend fun currentSettings(): AppSettings = settings.first()

    suspend fun completeOnboarding() {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    suspend fun replaceSettings(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = settings.language.name
            preferences[Keys.THEME_MODE] = settings.themeMode.name
            preferences[Keys.ACCENT_COLOR] = settings.accentColor.name
            preferences[Keys.USE_DYNAMIC_COLOR] = settings.useDynamicColor
            preferences[Keys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[Keys.GOAL_NOTIFICATIONS_ENABLED] = settings.goalNotificationsEnabled
            preferences[Keys.SUMMARY_CADENCE] = settings.summaryCadence.name
            preferences[Keys.SUMMARY_TIME] = settings.summaryTime.toString()
            val anchorDate = settings.summaryAnchorDate
            if (anchorDate != null) {
                preferences[Keys.SUMMARY_ANCHOR_DATE] = anchorDate.toString()
            } else {
                preferences.remove(Keys.SUMMARY_ANCHOR_DATE)
            }
        }
    }

    suspend fun setLanguage(value: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = value.name }
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
        context.settingsDataStore.edit { preferences ->
            val previous = preferences[Keys.SUMMARY_CADENCE]
            preferences[Keys.SUMMARY_CADENCE] = value.name

            if (value == SummaryCadence.OFF) {
                preferences.remove(Keys.SUMMARY_ANCHOR_DATE)
            } else if (previous != value.name || preferences[Keys.SUMMARY_ANCHOR_DATE] == null) {
                // Anchor weekly/monthly summaries to the day the cadence was enabled.
                preferences[Keys.SUMMARY_ANCHOR_DATE] = LocalDate.now().toString()
            }
        }
    }

    suspend fun setSummaryAnchorDate(value: LocalDate) {
        context.settingsDataStore.edit { it[Keys.SUMMARY_ANCHOR_DATE] = value.toString() }
    }

    suspend fun setSummaryTime(value: LocalTime) {
        context.settingsDataStore.edit { it[Keys.SUMMARY_TIME] = value.toString() }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val GOAL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("goal_notifications_enabled")
        val SUMMARY_CADENCE = stringPreferencesKey("summary_cadence")
        val SUMMARY_TIME = stringPreferencesKey("summary_time")
        val SUMMARY_ANCHOR_DATE = stringPreferencesKey("summary_anchor_date")
    }
}
