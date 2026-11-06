package id.my.f1zz.whenyah.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors

class SummaryWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val scheduledAt = inputData.getLong(ReminderWorkKeys.SCHEDULED_AT, 0L)

        val coordinator = EntryPointAccessors.fromApplication(
            applicationContext,
            ReminderWorkerEntryPoint::class.java,
        ).reminderCoordinator()

        runCatching { coordinator.handleSummary(scheduledAt) }
        return Result.success()
    }
}
