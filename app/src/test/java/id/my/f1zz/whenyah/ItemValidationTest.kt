package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.validation.ItemValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemValidationTest {
    @Test
    fun parseIdrAmount_acceptsWholeRupiah() {
        assertEquals(1_500_000L, ItemValidation.parseIdrAmount("1500000"))
        assertEquals(0L, ItemValidation.parseIdrAmount("0"))
    }

    @Test
    fun parseIdrAmount_rejectsInvalidOrFractionalInput() {
        assertNull(ItemValidation.parseIdrAmount(""))
        assertNull(ItemValidation.parseIdrAmount("  "))
        assertNull(ItemValidation.parseIdrAmount("-100"))
        assertNull(ItemValidation.parseIdrAmount("1.500.000"))
        assertNull(ItemValidation.parseIdrAmount("1,5"))
        assertNull(ItemValidation.parseIdrAmount("abc"))
        assertNull(ItemValidation.parseIdrAmount("99999999999999999999"))
    }

    @Test
    fun nameMustNotBeBlank() {
        assertFalse(ItemValidation.isValidName("   "))
        assertTrue(ItemValidation.isValidName("Mechanical keyboard"))
    }

    @Test
    fun purchaseUrl_allowsEmptyAndHttpSchemes() {
        assertTrue(ItemValidation.isValidPurchaseUrl(""))
        assertTrue(ItemValidation.isValidPurchaseUrl("https://example.com/item"))
        assertTrue(ItemValidation.isValidPurchaseUrl("http://example.com/item"))
    }

    @Test
    fun purchaseUrl_rejectsUnsafeOrMalformedValues() {
        assertFalse(ItemValidation.isValidPurchaseUrl("example.com/item"))
        assertFalse(ItemValidation.isValidPurchaseUrl("ftp://example.com/item"))
        assertFalse(ItemValidation.isValidPurchaseUrl("javascript:alert(1)"))
        assertFalse(ItemValidation.isValidPurchaseUrl("https://"))
    }
}
