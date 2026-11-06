package id.my.f1zz.whenyah.data.notification

internal object ReminderWorkKeys {
    const val PLAN_ID = "plan_id"
    const val SCHEDULED_AT = "scheduled_at"

    const val TAG_PLAN_REMINDER = "plan-reminder"
    const val TAG_SUMMARY = "summary-reminder"

    fun planItemTag(itemId: java.util.UUID): String = "plan-reminder-item-$itemId"
    fun planTag(planId: java.util.UUID): String = "plan-reminder-plan-$planId"
}
