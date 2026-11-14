package id.my.f1zz.whenyah.data.notification

import id.my.f1zz.whenyah.data.local.dao.SavingsPlanDao
import id.my.f1zz.whenyah.data.local.dao.WishlistItemDao
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.data.settings.SettingsRepository
import id.my.f1zz.whenyah.domain.dashboard.DashboardCalculator
import id.my.f1zz.whenyah.domain.dashboard.DashboardItem
import id.my.f1zz.whenyah.domain.i18n.LanguageStore
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.notifications.NotificationScheduleCalculator
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderCoordinator @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val savingsPlanDao: SavingsPlanDao,
    private val itemDao: WishlistItemDao,
    private val wishlistRepository: WishlistRepository,
    private val notificationService: NotificationService,
    private val alarmScheduler: ReminderAlarmScheduler,
    private val languageStore: LanguageStore,
) {
    suspend fun syncAllSchedules() {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled) {
            cancelAllSchedules()
            return
        }

        savingsPlanDao.getAllPlans()
            .map { it.itemId }
            .distinct()
            .forEach { itemId ->
                syncPlanReminderForItem(itemId)
            }

        syncSummarySchedule()
    }

    suspend fun syncPlanReminderForItem(itemId: UUID) {
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

        if (!eligible) {
            alarmScheduler.cancelPlanAlarm(itemId)
            return
        }

        val loadedPlan = checkNotNull(plan)
        val anchorDate = loadedPlan.reminderAnchorDate
            ?: loadedPlan.updatedAt.atZone(ZoneId.systemDefault()).toLocalDate()
        if (loadedPlan.reminderAnchorDate == null) {
            savingsPlanDao.upsert(loadedPlan.copy(reminderAnchorDate = anchorDate))
        }
        schedulePlanReminder(
            plan = loadedPlan,
            itemId = itemId,
            anchorDate = anchorDate,
        )
    }

    suspend fun cancelPlanReminderForItem(itemId: UUID) {
        alarmScheduler.cancelPlanAlarm(itemId)
    }

    suspend fun syncSummarySchedule() {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled || settings.summaryCadence == SummaryCadence.OFF) {
            alarmScheduler.cancelSummaryAlarm()
            return
        }

        val anchorDate = settings.summaryAnchorDate ?: run {
            val today = LocalDate.now()
            settingsRepository.setSummaryAnchorDate(today)
            today
        }
        scheduleSummaryReminder(
            cadence = settings.summaryCadence,
            time = settings.summaryTime,
            anchorDate = anchorDate,
        )
    }

    suspend fun cancelAllSchedules() {
        savingsPlanDao.getAllPlans()
            .map { it.itemId }
            .distinct()
            .forEach { itemId ->
                alarmScheduler.cancelPlanAlarm(itemId)
            }
        alarmScheduler.cancelSummaryAlarm()
    }

    suspend fun handlePlanReminder(planId: UUID, scheduledAtEpochMillis: Long) {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled) return
        languageStore.update(settings.language)

        val plan = savingsPlanDao.getById(planId) ?: return
        val item = itemDao.findById(plan.itemId) ?: return
        if (!plan.active || !plan.reminderEnabled || item.status != ItemStatus.ACTIVE) return

        if (notificationService.canPostNotifications()) {
            notificationService.showPlanReminder(
                itemId = item.id,
                planId = plan.id,
                itemName = item.name,
                contributionIdr = plan.contributionIdr,
                frequency = plan.frequency,
            )
        }

        // Keep the day the reminder was enabled as the anchor so weekly/monthly
        // schedules survive app restarts and monthly reminders stay clamped
        // (e.g. Jan 31 -> Feb 28 -> Mar 31).
        val anchorDate = plan.reminderAnchorDate
            ?: plan.updatedAt.atZone(ZoneId.systemDefault()).toLocalDate()
        if (plan.reminderAnchorDate == null) {
            savingsPlanDao.upsert(plan.copy(reminderAnchorDate = anchorDate))
        }
        schedulePlanReminder(
            plan = plan,
            itemId = item.id,
            anchorDate = anchorDate,
            now = schedulingNow(scheduledAtEpochMillis),
        )
    }

    suspend fun handleSummary(scheduledAtEpochMillis: Long) {
        val settings = settingsRepository.currentSettings()
        if (!settings.notificationsEnabled || settings.summaryCadence == SummaryCadence.OFF) return
        languageStore.update(settings.language)

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

        val anchorDate = settings.summaryAnchorDate ?: run {
            val firedDate = scheduledDate(scheduledAtEpochMillis)
            settingsRepository.setSummaryAnchorDate(firedDate)
            firedDate
        }
        scheduleSummaryReminder(
            cadence = settings.summaryCadence,
            time = settings.summaryTime,
            anchorDate = anchorDate,
            now = schedulingNow(scheduledAtEpochMillis),
        )
    }

    private fun schedulePlanReminder(
        plan: SavingsPlanEntity,
        itemId: UUID,
        anchorDate: LocalDate,
        now: ZonedDateTime = ZonedDateTime.now(),
    ) {
        val reminderTime = plan.reminderLocalTime ?: return
        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = plan.frequency,
            time = reminderTime,
            anchorDate = anchorDate,
        )
        alarmScheduler.schedulePlanAlarm(
            itemId = itemId,
            planId = plan.id,
            triggerAtMillis = next.toInstant().toEpochMilli(),
        )
    }

    private fun scheduleSummaryReminder(
        cadence: SummaryCadence,
        time: java.time.LocalTime,
        anchorDate: LocalDate,
        now: ZonedDateTime = ZonedDateTime.now(),
    ) {
        val next = NotificationScheduleCalculator.nextSummary(
            now = now,
            cadence = cadence,
            time = time,
            anchorDate = anchorDate,
        ) ?: run {
            alarmScheduler.cancelSummaryAlarm()
            return
        }

        alarmScheduler.scheduleSummaryAlarm(next.toInstant().toEpochMilli())
    }

    private fun scheduledDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    /**
     * If an inexact alarm fires before its requested time, advance the reference
     * point past that occurrence so it is not scheduled a second time.
     */
    private fun schedulingNow(occurrenceEpochMillis: Long): ZonedDateTime {
        val now = ZonedDateTime.now()
        val occurrence = Instant.ofEpochMilli(occurrenceEpochMillis).atZone(now.zone)
        return if (occurrence.isAfter(now)) occurrence.plusSeconds(1) else now
    }
}
