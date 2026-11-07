package id.my.f1zz.whenyah.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderAlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = checkNotNull(context.getSystemService(AlarmManager::class.java))

    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    fun schedulePlanAlarm(itemId: UUID, planId: UUID, triggerAtMillis: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderAlarmContract.planRequestCode(itemId),
            planIntent(itemId).apply {
                putExtra(ReminderAlarmContract.EXTRA_PLAN_ID, planId.toString())
                putExtra(ReminderAlarmContract.EXTRA_SCHEDULED_AT, triggerAtMillis)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(triggerAtMillis, pendingIntent)
    }

    fun cancelPlanAlarm(itemId: UUID) {
        cancel(
            requestCode = ReminderAlarmContract.planRequestCode(itemId),
            intent = planIntent(itemId),
        )
    }

    fun scheduleSummaryAlarm(triggerAtMillis: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderAlarmContract.SUMMARY_REQUEST_CODE,
            summaryIntent().apply {
                putExtra(ReminderAlarmContract.EXTRA_SCHEDULED_AT, triggerAtMillis)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(triggerAtMillis, pendingIntent)
    }

    fun cancelSummaryAlarm() {
        cancel(
            requestCode = ReminderAlarmContract.SUMMARY_REQUEST_CODE,
            intent = summaryIntent(),
        )
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            // Inexact, but still allowed to fire while the device is in Doze.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    private fun cancel(requestCode: Int, intent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun planIntent(itemId: UUID): Intent =
        Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmContract.ACTION_PLAN_REMINDER
            data = Uri.parse(ReminderAlarmContract.planData(itemId))
        }

    private fun summaryIntent(): Intent =
        Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmContract.ACTION_SUMMARY
            data = Uri.parse(ReminderAlarmContract.summaryData())
        }
}
