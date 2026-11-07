package id.my.f1zz.whenyah.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != ReminderAlarmContract.ACTION_PLAN_REMINDER &&
            action != ReminderAlarmContract.ACTION_SUMMARY
        ) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val coordinator = EntryPointAccessors.fromApplication(
                    appContext,
                    ReminderEntryPoint::class.java,
                ).reminderCoordinator()

                when (action) {
                    ReminderAlarmContract.ACTION_PLAN_REMINDER -> {
                        val planId = intent
                            .getStringExtra(ReminderAlarmContract.EXTRA_PLAN_ID)
                            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                        if (planId != null) {
                            coordinator.handlePlanReminder(
                                planId = planId,
                                scheduledAtEpochMillis = intent.getLongExtra(
                                    ReminderAlarmContract.EXTRA_SCHEDULED_AT,
                                    0L,
                                ),
                            )
                        }
                    }

                    ReminderAlarmContract.ACTION_SUMMARY -> {
                        coordinator.handleSummary(
                            scheduledAtEpochMillis = intent.getLongExtra(
                                ReminderAlarmContract.EXTRA_SCHEDULED_AT,
                                0L,
                            ),
                        )
                    }
                }
            } catch (failure: Throwable) {
                // Do not crash the app if a reminder cannot be delivered; the next
                // app start re-registers all alarms.
                Log.e(TAG, "Reminder alarm failed", failure)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "ReminderAlarmReceiver"
    }
}
