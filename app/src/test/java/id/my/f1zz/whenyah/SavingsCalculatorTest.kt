package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.savings.SavingsCalculator
import id.my.f1zz.whenyah.domain.savings.SavingsLedgerEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SavingsCalculatorTest {
    @Test
    fun balanceSumsDepositsMinusWithdrawalsAndPurchaseSpend() {
        val entries = listOf(
            deposit(1_000),
            deposit(500),
            withdrawal(300),
            purchaseSpend(200),
        )

        assertEquals(1_000L, SavingsCalculator.balance(entries))
    }

    @Test
    fun emptyLedgerHasZeroBalanceAndUnmetTarget() {
        val summary = SavingsCalculator.summarize(emptyList(), targetPriceIdr = 1_000)

        assertEquals(0L, summary.availableIdr)
        assertEquals(1_000L, summary.remainingIdr)
        assertEquals(0L, summary.surplusIdr)
        assertEquals(0f, summary.progress, 0.0001f)
        assertFalse(summary.isGoalReached)
    }

    @Test
    fun partialFundingReportsRemainingAndProgress() {
        val summary = SavingsCalculator.summarize(listOf(deposit(400)), targetPriceIdr = 1_000)

        assertEquals(400L, summary.availableIdr)
        assertEquals(600L, summary.remainingIdr)
        assertEquals(0L, summary.surplusIdr)
        assertEquals(0.4f, summary.progress, 0.0001f)
        assertFalse(summary.isGoalReached)
    }

    @Test
    fun exactFundingReachesGoal() {
        val summary = SavingsCalculator.summarize(listOf(deposit(1_000)), targetPriceIdr = 1_000)

        assertEquals(0L, summary.remainingIdr)
        assertEquals(1f, summary.progress, 0.0001f)
        assertTrue(summary.isGoalReached)
    }

    @Test
    fun overfundedItemKeepsSurplusButCapsProgress() {
        val summary = SavingsCalculator.summarize(listOf(deposit(1_500)), targetPriceIdr = 1_000)

        assertEquals(1_500L, summary.availableIdr)
        assertEquals(500L, summary.surplusIdr)
        assertEquals(1f, summary.progress, 0.0001f)
        assertTrue(summary.isGoalReached)
    }

    @Test
    fun zeroTargetIsGoalReachedWithoutDivisionByZero() {
        val summary = SavingsCalculator.summarize(emptyList(), targetPriceIdr = 0)

        assertEquals(0L, summary.availableIdr)
        assertEquals(0L, summary.remainingIdr)
        assertEquals(1f, summary.progress, 0.0001f)
        assertTrue(summary.isGoalReached)
    }

    @Test
    fun withdrawalValidationUsesDerivedBalance() {
        val entries = listOf(deposit(500), withdrawal(200))

        assertTrue(SavingsCalculator.canWithdraw(entries, 300))
        assertFalse(SavingsCalculator.canWithdraw(entries, 301))
        assertFalse(SavingsCalculator.canWithdraw(entries, 0))
        assertFalse(SavingsCalculator.canWithdraw(entries, -1))
    }

    @Test
    fun negativeTargetIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            SavingsCalculator.summarize(emptyList(), targetPriceIdr = -1)
        }
    }

    @Test
    fun balanceFailsSafelyOnOverflow() {
        assertThrows(ArithmeticException::class.java) {
            SavingsCalculator.balance(
                listOf(
                    deposit(Long.MAX_VALUE),
                    deposit(1),
                )
            )
        }
    }

    private fun deposit(amountIdr: Long): SavingsLedgerEntry =
        TestEntry(SavingsEventType.DEPOSIT, amountIdr)

    private fun withdrawal(amountIdr: Long): SavingsLedgerEntry =
        TestEntry(SavingsEventType.WITHDRAWAL, amountIdr)

    private fun purchaseSpend(amountIdr: Long): SavingsLedgerEntry =
        TestEntry(SavingsEventType.PURCHASE_SPEND, amountIdr)

    private data class TestEntry(
        override val type: SavingsEventType,
        override val amountIdr: Long,
    ) : SavingsLedgerEntry
}
