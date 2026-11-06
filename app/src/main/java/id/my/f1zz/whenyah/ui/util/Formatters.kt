package id.my.f1zz.whenyah.ui.util

import id.my.f1zz.whenyah.domain.format.formatIdr as domainFormatIdr
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatIdr(amount: Long): String = domainFormatIdr(amount)

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

fun formatTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
