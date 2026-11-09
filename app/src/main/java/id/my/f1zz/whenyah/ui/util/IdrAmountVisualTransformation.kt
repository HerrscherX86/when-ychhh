package id.my.f1zz.whenyah.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Shows grouped IDR digits while the underlying field value stays digits-only.
 */
class IdrAmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = formatIdrDigits(raw)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                formatIdrDigits(raw.take(offset.coerceIn(0, raw.length))).length

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset.coerceIn(0, formatted.length)).count(Char::isDigit)
        }

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = offsetMapping,
        )
    }
}
