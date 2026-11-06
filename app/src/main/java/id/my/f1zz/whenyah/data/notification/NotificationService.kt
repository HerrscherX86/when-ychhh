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
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun ensureChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDERS,
                "Contribution reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Reminders for savings-plan contributions"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GOALS,
                "Goal reached",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifications when an item reaches its savings goal"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SUMMARIES,
                "Progress summaries",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Periodic savings progress summaries"
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
        val cadence = when (frequency) {
            PlanFrequency.DAILY -> "daily"
            PlanFrequency.WEEKLY -> "weekly"
            PlanFrequency.MONTHLY -> "monthly"
        }
        post(
            channelId = CHANNEL_REMINDERS,
            notificationId = planId.hashCode(),
            title = "Time to save for $itemName",
            message = "${formatIdr(contributionIdr)} $cadence contribution. " +
                "Planned contributions are not deducted automatically.",
        )
    }

    fun showGoalReached(
        itemId: UUID,
        itemName: String,
        targetIdr: Long,
        savedIdr: Long,
    ) {
        post(
            channelId = CHANNEL_GOALS,
            notificationId = GOAL_NOTIFICATION_BASE + itemId.hashCode(),
            title = "Goal reached: $itemName",
            message = "Saved ${formatIdr(savedIdr)} of ${formatIdr(targetIdr)}.",
        )
    }

    fun showSummary(
        activeItemCount: Int,
        totalSavedIdr: Long,
        fullyFundedCount: Int,
    ) {
        post(
            channelId = CHANNEL_SUMMARIES,
            notificationId = SUMMARY_NOTIFICATION_ID,
            title = "Savings progress",
            message = "$activeItemCount active item${if (activeItemCount == 1) "" else "s"} • " +
                "${formatIdr(totalSavedIdr)} saved • $fullyFundedCount funded",
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
    }
}
