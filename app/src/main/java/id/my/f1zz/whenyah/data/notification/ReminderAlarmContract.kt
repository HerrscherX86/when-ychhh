package id.my.f1zz.whenyah.data.notification

import java.util.UUID

internal object ReminderAlarmContract {
    const val ACTION_PLAN_REMINDER = "id.my.f1zz.whenyah.action.PLAN_REMINDER"
    const val ACTION_SUMMARY = "id.my.f1zz.whenyah.action.SUMMARY"

    const val EXTRA_PLAN_ID = "plan_id"
    const val EXTRA_SCHEDULED_AT = "scheduled_at"

    const val SUMMARY_REQUEST_CODE = 7331

    fun planData(itemId: UUID): String = "whenyah://plan-reminder/$itemId"

    fun summaryData(): String = "whenyah://summary-reminder"

    fun planRequestCode(itemId: UUID): Int = itemId.hashCode()
}
