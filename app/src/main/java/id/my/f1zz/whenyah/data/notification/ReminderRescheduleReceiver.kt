package id.my.f1zz.whenyah.data.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!isRescheduleAction(intent.action)) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                EntryPointAccessors.fromApplication(
                    appContext,
                    ReminderEntryPoint::class.java,
                ).reminderCoordinator().syncAllSchedules()
            } catch (failure: Throwable) {
                Log.e(TAG, "Could not restore reminder alarms", failure)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isRescheduleAction(action: String?): Boolean {
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return true
        }
        return action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
    }

    private companion object {
        const val TAG = "ReminderReschedule"
    }
}
