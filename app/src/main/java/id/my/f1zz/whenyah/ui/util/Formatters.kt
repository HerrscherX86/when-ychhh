package id.my.f1zz.whenyah.ui.util

import id.my.f1zz.whenyah.domain.format.formatIdr as domainFormatIdr
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatIdr(amount: Long): String = domainFormatIdr(amount)

fun formatDateTime(
    instant: Instant,
    locale: Locale = Locale.getDefault(),
): String =
    instant
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", locale))

fun formatShortDate(
    instant: Instant,
    locale: Locale = Locale.getDefault(),
): String =
    instant
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM", locale))

fun formatDate(
    date: LocalDate,
    locale: Locale = Locale.getDefault(),
): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

fun formatTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
