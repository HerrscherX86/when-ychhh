package id.my.f1zz.whenyah

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import id.my.f1zz.whenyah.data.notification.NotificationService
import id.my.f1zz.whenyah.data.notification.ReminderCoordinator
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

    override fun onCreate() {
        super.onCreate()
        notificationService.ensureChannels()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            reminderCoordinator.syncAllSchedules(force = false)
        }
    }
}
