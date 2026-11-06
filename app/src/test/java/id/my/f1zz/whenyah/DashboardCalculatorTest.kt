package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.dashboard.DashboardCalculator
import id.my.f1zz.whenyah.domain.dashboard.DashboardItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DashboardCalculatorTest {
    @Test
    fun emptyDashboardHasZeroTotals() {
        val summary = DashboardCalculator.summarize(emptyList())

        assertEquals(0, summary.activeItemCount)
        assertEquals(0L, summary.totalTargetIdr)
        assertEquals(0L, summary.totalAvailableIdr)
        assertEquals(0, summary.fullyFundedCount)
        assertEquals(0f, summary.overallProgress, 0.0001f)
    }

    @Test
    fun sumsTargetsSavingsAndFundedCount() {
        val summary = DashboardCalculator.summarize(
            listOf(
                DashboardItem(targetPriceIdr = 1_000, availableIdr = 500),
                DashboardItem(targetPriceIdr = 2_000, availableIdr = 2_500),
                DashboardItem(targetPriceIdr = 500, availableIdr = 0),
            )
        )

        assertEquals(3, summary.activeItemCount)
        assertEquals(3_500L, summary.totalTargetIdr)
        assertEquals(3_000L, summary.totalAvailableIdr)
        assertEquals(1, summary.fullyFundedCount)
        assertEquals(3_000f / 3_500f, summary.overallProgress, 0.0001f)
    }

    @Test
    fun overallProgressCapsAtOne() {
        val summary = DashboardCalculator.summarize(
            listOf(DashboardItem(targetPriceIdr = 1_000, availableIdr = 5_000))
        )

        assertEquals(1f, summary.overallProgress, 0.0001f)
        assertEquals(1, summary.fullyFundedCount)
    }

    @Test
    fun zeroTargetItemCountsAsFullyFunded() {
        val summary = DashboardCalculator.summarize(
            listOf(DashboardItem(targetPriceIdr = 0, availableIdr = 0))
        )

        assertEquals(1, summary.fullyFundedCount)
        assertEquals(1f, summary.overallProgress, 0.0001f)
    }

    @Test
    fun zeroTotalTargetWithItemsReportsComplete() {
        val summary = DashboardCalculator.summarize(
            listOf(
                DashboardItem(targetPriceIdr = 0, availableIdr = 0),
                DashboardItem(targetPriceIdr = 0, availableIdr = 100),
            )
        )

        assertEquals(0L, summary.totalTargetIdr)
        assertEquals(1f, summary.overallProgress, 0.0001f)
        assertEquals(2, summary.fullyFundedCount)
    }

    @Test
    fun aggregationOverflowFailsSafely() {
        assertThrows(ArithmeticException::class.java) {
            DashboardCalculator.summarize(
                listOf(
                    DashboardItem(targetPriceIdr = Long.MAX_VALUE, availableIdr = 0),
                    DashboardItem(targetPriceIdr = 1, availableIdr = 0),
                )
            )
        }
    }

    @Test
    fun negativeValuesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            DashboardCalculator.summarize(
                listOf(DashboardItem(targetPriceIdr = -1, availableIdr = 0))
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DashboardCalculator.summarize(
                listOf(DashboardItem(targetPriceIdr = 1, availableIdr = -1))
            )
        }
    }
}
