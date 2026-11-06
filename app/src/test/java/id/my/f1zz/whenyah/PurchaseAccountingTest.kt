package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.purchase.PurchaseAccounting
import id.my.f1zz.whenyah.domain.savings.SavingsCalculator
import id.my.f1zz.whenyah.domain.savings.SavingsLedgerEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseAccountingTest {
    @Test
    fun purchaseSpendsEntireAvailableBalance() {
        val spent = PurchaseAccounting.plan(1_200)

        assertEquals(1_200L, spent.savingsSpentIdr)
        assertTrue(spent.shouldWriteSpendEvent)
    }

    @Test
    fun zeroBalanceSkipsSpendEvent() {
        val spent = PurchaseAccounting.plan(0)

        assertEquals(0L, spent.savingsSpentIdr)
        assertFalse(spent.shouldWriteSpendEvent)
    }

    @Test
    fun negativeBalanceIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            PurchaseAccounting.plan(-1)
        }
    }

    @Test
    fun purchaseSpendZerosBalanceWithoutErasingHistory() {
        val entries = listOf(
            TestEntry(SavingsEventType.DEPOSIT, 1_200),
            TestEntry(SavingsEventType.WITHDRAWAL, 200),
            TestEntry(SavingsEventType.PURCHASE_SPEND, 1_000),
        )

        assertEquals(0L, SavingsCalculator.balance(entries))
        assertEquals(3, entries.size)
        assertEquals(1_200L, entries.first().amountIdr)
    }

    @Test
    fun overfundedPurchaseSpendsTheSurplusToo() {
        val entries = listOf(
            TestEntry(SavingsEventType.DEPOSIT, 1_500),
            TestEntry(SavingsEventType.PURCHASE_SPEND, 1_500),
        )

        assertEquals(0L, SavingsCalculator.balance(entries))
    }

    private data class TestEntry(
        override val type: SavingsEventType,
        override val amountIdr: Long,
    ) : SavingsLedgerEntry
}
