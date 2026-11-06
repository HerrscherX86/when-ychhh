package id.my.f1zz.whenyah.ui.util

import java.text.NumberFormat
import java.time.Instant
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
