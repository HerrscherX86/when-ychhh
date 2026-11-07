package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.ui.util.formatIdrDigits
import org.junit.Assert.assertEquals
import org.junit.Test

class IdrInputFormattingTest {
    @Test
    fun groupsRupiahDigitsWithDots() {
        assertEquals("0", formatIdrDigits("0"))
        assertEquals("999", formatIdrDigits("999"))
        assertEquals("1.000", formatIdrDigits("1000"))
        assertEquals("1.500.000", formatIdrDigits("1500000"))
        assertEquals("123.456.789", formatIdrDigits("123456789"))
    }

    @Test
    fun emptyInputStaysEmpty() {
        assertEquals("", formatIdrDigits(""))
    }
}
