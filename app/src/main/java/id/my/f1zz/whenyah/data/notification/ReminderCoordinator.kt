package id.my.f1zz.whenyah.data.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import id.my.f1zz.whenyah.data.local.dao.SavingsPlanDao
import id.my.f1zz.whenyah.data.local.dao.WishlistItemDao
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.data.settings.SettingsRepository
import id.my.f1zz.whenyah.domain.dashboard.DashboardCalculator
import id.my.f1zz.whenyah.domain.dashboard.DashboardItem
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.notifications.NotificationScheduleCalculator
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val savingsPlanDao: SavingsPlanDao,
    private val itemDao: WishlistItemDao,
    private val wishlistRepository: WishlistRepository,
    private val notificationService: NotificationService,
) {
    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    suspend fun syncAllSchedules(force: Boolean) {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled) {
            cancelAllSchedules()
            return
        }

        if (force) {
            workManager.cancelAllWorkByTag(ReminderWorkKeys.TAG_PLAN_REMINDER)
        }

        savingsPlanDao.getAllPlans()
            .map { it.itemId }
            .distinct()
            .forEach { itemId ->
                syncPlanReminderForItem(itemId, force = false)
            }

        syncSummarySchedule(force = force)
    }

    suspend fun syncPlanReminderForItem(itemId: UUID, force: Boolean) {
        val settings = settingsRepository.currentSettings()
        val plan = savingsPlanDao.getByItem(itemId)
        val item = itemDao.findById(itemId)
        val eligible = settings.notificationsEnabled &&
            plan != null &&
            plan.active &&
            plan.reminderEnabled &&
            plan.reminderLocalTime != null &&
            item != null &&
            item.status == ItemStatus.ACTIVE
        val itemTag = ReminderWorkKeys.planItemTag(itemId)

        if (!eligible) {
            workManager.cancelAllWorkByTag(itemTag)
            return
        }

        if (!force && hasPendingWork(itemTag)) {
            return
        }

        workManager.cancelAllWorkByTag(itemTag)
        enqueuePlanReminder(
            plan = checkNotNull(plan),
            itemId = itemId,
            itemName = checkNotNull(item).name,
            anchorDate = LocalDate.now(),
        )
    }

    suspend fun cancelPlanReminderForItem(itemId: UUID) {
        workManager.cancelAllWorkByTag(ReminderWorkKeys.planItemTag(itemId))
    }

    suspend fun syncSummarySchedule(force: Boolean) {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled || settings.summaryCadence == SummaryCadence.OFF) {
            workManager.cancelAllWorkByTag(ReminderWorkKeys.TAG_SUMMARY)
            return
        }

        if (!force && hasPendingWork(ReminderWorkKeys.TAG_SUMMARY)) {
            return
        }

        workManager.cancelAllWorkByTag(ReminderWorkKeys.TAG_SUMMARY)
        enqueueSummaryReminder(
            cadence = settings.summaryCadence,
            time = settings.summaryTime,
            anchorDate = LocalDate.now(),
        )
    }

    suspend fun cancelAllSchedules() {
        workManager.cancelAllWorkByTag(ReminderWorkKeys.TAG_PLAN_REMINDER)
        workManager.cancelAllWorkByTag(ReminderWorkKeys.TAG_SUMMARY)
    }

    suspend fun handlePlanReminder(planId: UUID, scheduledAtEpochMillis: Long) {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled) return

        val plan = savingsPlanDao.getById(planId) ?: return
        val item = itemDao.findById(plan.itemId) ?: return
        if (!plan.active || !plan.reminderEnabled || item.status != ItemStatus.ACTIVE) return

        if (notificationService.canPostNotifications()) {
            notificationService.showPlanReminder(
                planId = plan.id,
                itemName = item.name,
                contributionIdr = plan.contributionIdr,
                frequency = plan.frequency,
            )
        }

        enqueuePlanReminder(
            plan = plan,
            itemId = item.id,
            itemName = item.name,
            anchorDate = scheduledDate(scheduledAtEpochMillis),
        )
    }

    suspend fun handleSummary(scheduledAtEpochMillis: Long) {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled || settings.summaryCadence == SummaryCadence.OFF) return

        val activeItems = wishlistRepository.observeActiveItemsWithSavings().first()
        val summary = DashboardCalculator.summarize(
            activeItems.map {
                DashboardItem(
                    targetPriceIdr = it.item.targetPriceIdr,
                    availableIdr = it.savings.availableIdr,
                )
            }
        )

        if (summary.activeItemCount > 0 && notificationService.canPostNotifications()) {
            notificationService.showSummary(
                activeItemCount = summary.activeItemCount,
                totalSavedIdr = summary.totalAvailableIdr,
                fullyFundedCount = summary.fullyFundedCount,
            )
        }

        enqueueSummaryReminder(
            cadence = settings.summaryCadence,
            time = settings.summaryTime,
            anchorDate = scheduledDate(scheduledAtEpochMillis),
        )
    }

    private fun enqueuePlanReminder(
        plan: id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity,
        itemId: UUID,
        itemName: String,
        anchorDate: LocalDate,
    ) {
        val reminderTime = plan.reminderLocalTime ?: return
        val now = ZonedDateTime.now()
        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = plan.frequency,
            time = reminderTime,
            anchorDate = anchorDate,
        )
        val delayMillis = Duration.between(now, next).toMillis().coerceAtLeast(0)
        val scheduledAt = next.toInstant().toEpochMilli()

        val request = OneTimeWorkRequestBuilder<PlanReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorkKeys.PLAN_ID to plan.id.toString(),
                    ReminderWorkKeys.SCHEDULED_AT to scheduledAt,
                )
            )
            .addTag(ReminderWorkKeys.TAG_PLAN_REMINDER)
            .addTag(ReminderWorkKeys.planItemTag(itemId))
            .addTag(ReminderWorkKeys.planTag(plan.id))
            .build()

        workManager.enqueueUniqueWork(
            "plan-reminder-${plan.id}-$scheduledAt",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private fun enqueueSummaryReminder(
        cadence: SummaryCadence,
        time: java.time.LocalTime,
        anchorDate: LocalDate,
    ) {
        val now = ZonedDateTime.now()
        val next = NotificationScheduleCalculator.nextSummary(
            now = now,
            cadence = cadence,
            time = time,
            anchorDate = anchorDate,
        ) ?: return
        val delayMillis = Duration.between(now, next).toMillis().coerceAtLeast(0)
        val scheduledAt = next.toInstant().toEpochMilli()

        val request = OneTimeWorkRequestBuilder<SummaryWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorkKeys.SCHEDULED_AT to scheduledAt,
                )
            )
            .addTag(ReminderWorkKeys.TAG_SUMMARY)
            .build()

        workManager.enqueueUniqueWork(
            "summary-$scheduledAt",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private suspend fun hasPendingWork(tag: String): Boolean = withContext(Dispatchers.IO) {
        workManager.getWorkInfosByTag(tag).get().any { !it.state.isFinished }
    }

    private fun scheduledDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
}
