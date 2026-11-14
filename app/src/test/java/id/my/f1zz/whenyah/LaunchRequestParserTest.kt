package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.data.navigation.LaunchRequest
import id.my.f1zz.whenyah.data.navigation.LaunchRequestParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class LaunchRequestParserTest {
    private val itemId = UUID.randomUUID()

    @Test
    fun `valid item with deposit opens the item with the amount`() {
        val request = LaunchRequestParser.parse(
            itemId = itemId.toString(),
            depositAmountIdr = 25_000,
            openAddItem = false,
        )

        assertEquals(
            LaunchRequest.OpenItem(itemId = itemId, depositAmountIdr = 25_000),
            request,
        )
    }

    @Test
    fun `valid item without deposit opens the item without a dialog`() {
        val request = LaunchRequestParser.parse(
            itemId = itemId.toString(),
            depositAmountIdr = null,
            openAddItem = false,
        )

        assertEquals(
            LaunchRequest.OpenItem(itemId = itemId, depositAmountIdr = null),
            request,
        )
    }

    @Test
    fun `non-positive deposit amounts are ignored`() {
        val zero = LaunchRequestParser.parse(itemId.toString(), 0, false)
        val negative = LaunchRequestParser.parse(itemId.toString(), -5, false)

        assertEquals(LaunchRequest.OpenItem(itemId, null), zero)
        assertEquals(LaunchRequest.OpenItem(itemId, null), negative)
    }

    @Test
    fun `invalid or missing item id produces no request`() {
        assertNull(LaunchRequestParser.parse("not-a-uuid", 10_000, false))
        assertNull(LaunchRequestParser.parse(null, 10_000, false))
        assertNull(LaunchRequestParser.parse("", null, false))
    }

    @Test
    fun `add item flag takes precedence`() {
        val request = LaunchRequestParser.parse(
            itemId = "not-a-uuid",
            depositAmountIdr = 10_000,
            openAddItem = true,
        )

        assertEquals(LaunchRequest.AddItem, request)
    }
}
