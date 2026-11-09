package id.my.f1zz.whenyah.data.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import id.my.f1zz.whenyah.MainActivity
import id.my.f1zz.whenyah.R
import id.my.f1zz.whenyah.domain.format.formatIdr
import id.my.f1zz.whenyah.domain.i18n.LanguageStore
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val languageStore: LanguageStore,
) {
    fun ensureChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val strings = languageStore.strings()

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDERS,
                strings.notificationChannelReminders,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = strings.notificationChannelRemindersDescription
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GOALS,
                strings.notificationChannelGoals,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = strings.notificationChannelGoalsDescription
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SUMMARIES,
                strings.notificationChannelSummaries,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = strings.notificationChannelSummariesDescription
            }
        )
    }

    fun canPostNotifications(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showPlanReminder(
        planId: UUID,
        itemName: String,
        contributionIdr: Long,
        frequency: PlanFrequency,
    ) {
        val strings = languageStore.strings()
        post(
            channelId = CHANNEL_REMINDERS,
            notificationId = planId.hashCode(),
            title = strings.planReminderTitle(itemName),
            message = strings.planReminderMessage(
                formatIdr(contributionIdr),
                frequency,
            ),
        )
    }

    fun showGoalReached(
        itemId: UUID,
        itemName: String,
        targetIdr: Long,
        savedIdr: Long,
    ) {
        val strings = languageStore.strings()
        post(
            channelId = CHANNEL_GOALS,
            notificationId = GOAL_NOTIFICATION_BASE + itemId.hashCode(),
            title = strings.goalNotificationTitle(itemName),
            message = strings.goalNotificationMessage(
                formatIdr(savedIdr),
                formatIdr(targetIdr),
            ),
        )
    }

    fun showTestNotification(): Boolean {
        if (!canPostNotifications()) return false
        val strings = languageStore.strings()
        post(
            channelId = CHANNEL_REMINDERS,
            notificationId = TEST_NOTIFICATION_ID,
            title = strings.testNotificationTitle,
            message = strings.testNotificationMessage,
        )
        return true
    }

    fun showSummary(
        activeItemCount: Int,
        totalSavedIdr: Long,
        fullyFundedCount: Int,
    ) {
        val strings = languageStore.strings()
        post(
            channelId = CHANNEL_SUMMARIES,
            notificationId = SUMMARY_NOTIFICATION_ID,
            title = strings.summaryNotificationTitle,
            message = strings.summaryNotificationMessage(
                activeItemCount,
                formatIdr(totalSavedIdr),
                fullyFundedCount,
            ),
        )
    }

    @SuppressLint("MissingPermission")
    private fun post(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
    ) {
        ensureChannels()
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val CHANNEL_REMINDERS = "plan_reminders"
        const val CHANNEL_GOALS = "goal_reached"
        const val CHANNEL_SUMMARIES = "progress_summaries"
        const val GOAL_NOTIFICATION_BASE = 100_000
        const val SUMMARY_NOTIFICATION_ID = 5001
        const val TEST_NOTIFICATION_ID = 5002
    }
}
