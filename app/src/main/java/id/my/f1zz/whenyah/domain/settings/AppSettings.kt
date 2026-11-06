package id.my.f1zz.whenyah.domain.settings

import id.my.f1zz.whenyah.domain.i18n.AppLanguage
import java.time.LocalTime

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AccentColor {
    DEFAULT,
    BLUE,
    GREEN,
    AMBER,
    ROSE,
}

enum class SummaryCadence {
    OFF,
    WEEKLY,
    MONTHLY,
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val useDynamicColor: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val goalNotificationsEnabled: Boolean = true,
    val summaryCadence: SummaryCadence = SummaryCadence.OFF,
    val summaryTime: LocalTime = LocalTime.of(9, 0),
)
