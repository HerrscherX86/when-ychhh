package id.my.f1zz.whenyah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import id.my.f1zz.whenyah.domain.i18n.AppStringsFactory
import id.my.f1zz.whenyah.ui.MainViewModel
import id.my.f1zz.whenyah.ui.WhenYahApp
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.WhenYahTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val strings = AppStringsFactory.forLanguage(settings.language)
            CompositionLocalProvider(LocalAppStrings provides strings) {
                WhenYahTheme(
                    themeMode = settings.themeMode,
                    accentColor = settings.accentColor,
                    dynamicColor = settings.useDynamicColor,
                ) {
                    WhenYahApp()
                }
            }
        }
    }
}
