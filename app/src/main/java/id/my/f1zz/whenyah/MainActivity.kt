package id.my.f1zz.whenyah

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import id.my.f1zz.whenyah.data.navigation.AppLauncher
import id.my.f1zz.whenyah.data.navigation.LaunchContract
import id.my.f1zz.whenyah.data.navigation.LaunchRequest
import id.my.f1zz.whenyah.data.navigation.LaunchRequestParser
import id.my.f1zz.whenyah.data.update.UpdateManager
import id.my.f1zz.whenyah.data.widget.WidgetUpdater
import id.my.f1zz.whenyah.domain.i18n.AppStringsFactory
import id.my.f1zz.whenyah.ui.MainViewModel
import id.my.f1zz.whenyah.ui.WhenYahApp
import id.my.f1zz.whenyah.ui.components.UpdateAvailableDialog
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.onboarding.OnboardingScreen
import id.my.f1zz.whenyah.ui.theme.WhenYahTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var updateManager: UpdateManager

    @Inject
    lateinit var appLauncher: AppLauncher

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            handleLaunchIntent(intent)
        }
        lifecycleScope.launch {
            widgetUpdater.refresh()
        }
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
            val pendingUpdate by updateManager.pendingUpdate.collectAsStateWithLifecycle()
            val strings = AppStringsFactory.forLanguage(settings.language)
            val context = LocalContext.current
            val updateScope = rememberCoroutineScope()

            CompositionLocalProvider(LocalAppStrings provides strings) {
                WhenYahTheme(
                    themeMode = settings.themeMode,
                    accentColor = settings.accentColor,
                    dynamicColor = settings.useDynamicColor,
                ) {
                    when (onboardingCompleted) {
                        null -> {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        false -> {
                            OnboardingScreen(
                                language = settings.language,
                                onLanguageChange = viewModel::setLanguage,
                                onFinish = viewModel::completeOnboarding,
                            )
                        }

                        true -> WhenYahApp(launcher = appLauncher)
                    }

                    pendingUpdate?.let { update ->
                        if (onboardingCompleted == true) {
                            UpdateAvailableDialog(
                                manifest = update,
                                strings = strings,
                                onUpdate = {
                                    updateManager.dismissPendingUpdate()
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        update.releaseUrl.toUri(),
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    runCatching { context.startActivity(intent) }
                                        .onFailure {
                                            Toast.makeText(
                                                context,
                                                strings.noAppCanOpenLink,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                },
                                onLater = updateManager::dismissPendingUpdate,
                                onSkip = {
                                    updateScope.launch {
                                        updateManager.skipPendingUpdate()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        val request = LaunchRequestParser.parse(
            itemId = intent?.getStringExtra(LaunchContract.EXTRA_ITEM_ID),
            depositAmountIdr = intent?.let {
                if (it.hasExtra(LaunchContract.EXTRA_DEPOSIT_IDR)) {
                    it.getLongExtra(LaunchContract.EXTRA_DEPOSIT_IDR, 0L)
                } else {
                    null
                }
            },
            openAddItem = intent?.getBooleanExtra(LaunchContract.EXTRA_OPEN_ADD_ITEM, false) == true,
        )

        when (request) {
            is LaunchRequest.OpenItem -> appLauncher.openItem(
                itemId = request.itemId,
                depositAmountIdr = request.depositAmountIdr,
            )

            LaunchRequest.AddItem -> appLauncher.addItem()
            null -> Unit
        }
    }
}
