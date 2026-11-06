package id.my.f1zz.whenyah.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.ThemeMode

private val AccentBlueLight = Color(0xFF1565C0)
private val AccentBlueDark = Color(0xFF90CAF9)
private val AccentGreenLight = Color(0xFF2E7D32)
private val AccentGreenDark = Color(0xFFA5D6A7)
private val AccentAmberLight = Color(0xFF8D6E00)
private val AccentAmberDark = Color(0xFFFFD54F)
private val AccentRoseLight = Color(0xFF9C3D6B)
private val AccentRoseDark = Color(0xFFF48FB1)

private fun accentPrimary(accentColor: AccentColor, darkTheme: Boolean): Color =
    when (accentColor) {
        AccentColor.DEFAULT -> if (darkTheme) Purple80 else Purple40
        AccentColor.BLUE -> if (darkTheme) AccentBlueDark else AccentBlueLight
        AccentColor.GREEN -> if (darkTheme) AccentGreenDark else AccentGreenLight
        AccentColor.AMBER -> if (darkTheme) AccentAmberDark else AccentAmberLight
        AccentColor.ROSE -> if (darkTheme) AccentRoseDark else AccentRoseLight
    }

private fun colorSchemeFor(accentColor: AccentColor, darkTheme: Boolean) =
    if (darkTheme) {
        darkColorScheme(
            primary = accentPrimary(accentColor, darkTheme = true),
            secondary = PurpleGrey80,
            tertiary = Pink80,
        )
    } else {
        lightColorScheme(
            primary = accentPrimary(accentColor, darkTheme = false),
            secondary = PurpleGrey40,
            tertiary = Pink40,
        )
    }

@Composable
fun WhenYahTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.DEFAULT,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        else -> colorSchemeFor(accentColor, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
