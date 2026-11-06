package id.my.f1zz.whenyah.data.notification

import dagger.hilt.android.qualifiers.ApplicationContext
import id.my.f1zz.whenyah.data.settings.SettingsRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalNotifier @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationService: NotificationService,
) {
    suspend fun notifyGoalReached(
        itemId: UUID,
        itemName: String,
        targetIdr: Long,
        savedIdr: Long,
    ) {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled || !settings.goalNotificationsEnabled) return
        if (!notificationService.canPostNotifications()) return

        notificationService.showGoalReached(
            itemId = itemId,
            itemName = itemName,
            targetIdr = targetIdr,
            savedIdr = savedIdr,
        )
    }
}
