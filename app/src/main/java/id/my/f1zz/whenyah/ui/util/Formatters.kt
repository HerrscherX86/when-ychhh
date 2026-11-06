package id.my.f1zz.whenyah.ui.util

import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idrNumberFormat: NumberFormat =
    NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))

fun formatIdr(amount: Long): String = "Rp ${idrNumberFormat.format(amount)}"

fun formatDateTime(instant: Instant): String =
    instant
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault()))

fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

fun formatPlanFrequency(frequency: PlanFrequency): String = when (frequency) {
    PlanFrequency.DAILY -> "Daily"
    PlanFrequency.WEEKLY -> "Weekly"
    PlanFrequency.MONTHLY -> "Monthly"
}
