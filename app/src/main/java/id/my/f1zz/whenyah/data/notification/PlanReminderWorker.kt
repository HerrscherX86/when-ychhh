package id.my.f1zz.whenyah.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import java.util.UUID

class PlanReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val planId = inputData.getString(ReminderWorkKeys.PLAN_ID)
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return Result.success()
        val scheduledAt = inputData.getLong(ReminderWorkKeys.SCHEDULED_AT, 0L)

        val coordinator = EntryPointAccessors.fromApplication(
            applicationContext,
            ReminderWorkerEntryPoint::class.java,
        ).reminderCoordinator()

        runCatching { coordinator.handlePlanReminder(planId, scheduledAt) }
        return Result.success()
    }
}
