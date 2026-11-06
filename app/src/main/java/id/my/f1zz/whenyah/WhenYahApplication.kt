package id.my.f1zz.whenyah

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import id.my.f1zz.whenyah.data.notification.NotificationService
import id.my.f1zz.whenyah.data.notification.ReminderCoordinator
import id.my.f1zz.whenyah.data.settings.SettingsRepository
import id.my.f1zz.whenyah.domain.i18n.LanguageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WhenYahApplication : Application() {
    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var reminderCoordinator: ReminderCoordinator

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var languageStore: LanguageStore

    override fun onCreate() {
        super.onCreate()
        notificationService.ensureChannels()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            settingsRepository.settings.collect { settings ->
                languageStore.update(settings.language)
            }
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            reminderCoordinator.syncAllSchedules(force = false)
        }
    }
}
