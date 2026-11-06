package id.my.f1zz.whenyah.domain.format

import java.text.NumberFormat
import java.util.Locale

private val idrNumberFormat: NumberFormat =
    NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))

fun formatIdr(amount: Long): String = "Rp ${idrNumberFormat.format(amount)}"
