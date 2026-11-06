package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.plan.SavingsPlanCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SavingsPlanCalculatorTest {
    private val start = LocalDate.of(2026, 11, 6)

    @Test
    fun alreadyFunded_isReachedTodayWithNoContributions() {
        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = 1_000,
            availableBalanceIdr = 1_100,
            contributionIdr = 100,
            frequency = PlanFrequency.WEEKLY,
            startDate = start,
        )

        assertEquals(0L, projection.remainingIdr)
        assertEquals(0L, projection.requiredContributions)
        assertEquals(start, projection.completionDate)
        assertTrue(projection.isGoalReached)
    }

    @Test
    fun oneContribution_completesToday() {
        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = 1_000,
            availableBalanceIdr = 900,
            contributionIdr = 100,
            frequency = PlanFrequency.WEEKLY,
            startDate = start,
        )

        assertEquals(100L, projection.remainingIdr)
        assertEquals(1L, projection.requiredContributions)
        assertEquals(start, projection.completionDate)
        assertFalse(projection.isGoalReached)
    }

    @Test
    fun threeWeeklyContributions_completeAfterTwoWeeks() {
        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = 1_000,
            availableBalanceIdr = 500,
            contributionIdr = 200,
            frequency = PlanFrequency.WEEKLY,
            startDate = start,
        )

        assertEquals(500L, projection.remainingIdr)
        assertEquals(3L, projection.requiredContributions)
        assertEquals(start.plusDays(14), projection.completionDate)
        assertFalse(projection.isGoalReached)
    }

    @Test
    fun zeroTarget_isReachedTodayWithoutDivisionByZero() {
        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = 0,
            availableBalanceIdr = 0,
            contributionIdr = 100,
            frequency = PlanFrequency.DAILY,
            startDate = start,
        )

        assertEquals(0L, projection.remainingIdr)
        assertEquals(0L, projection.requiredContributions)
        assertEquals(start, projection.completionDate)
        assertTrue(projection.isGoalReached)
    }

    @Test
    fun monthlyJanuary31_thirdContributionIsMarch31() {
        val january31 = LocalDate.of(2026, 1, 31)

        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = 300,
            availableBalanceIdr = 0,
            contributionIdr = 100,
            frequency = PlanFrequency.MONTHLY,
            startDate = january31,
        )

        assertEquals(3L, projection.requiredContributions)
        assertEquals(LocalDate.of(2026, 3, 31), projection.completionDate)
    }

    @Test
    fun monthlyAnchorClampsEachMonthButReturnsToAnchorDay() {
        val january31 = LocalDate.of(2026, 1, 31)

        assertEquals(
            LocalDate.of(2026, 2, 28),
            SavingsPlanCalculator.occurrenceDate(january31, PlanFrequency.MONTHLY, 1),
        )
        assertEquals(
            LocalDate.of(2026, 3, 31),
            SavingsPlanCalculator.occurrenceDate(january31, PlanFrequency.MONTHLY, 2),
        )
    }

    @Test
    fun monthlyLeapYearClampsToFebruary29() {
        val january31 = LocalDate.of(2028, 1, 31)

        assertEquals(
            LocalDate.of(2028, 2, 29),
            SavingsPlanCalculator.occurrenceDate(january31, PlanFrequency.MONTHLY, 1),
        )
        assertEquals(
            LocalDate.of(2028, 3, 31),
            SavingsPlanCalculator.occurrenceDate(january31, PlanFrequency.MONTHLY, 2),
        )
    }

    @Test
    fun monthlyYearBoundaryClampsToFebruaryEnd() {
        val december31 = LocalDate.of(2026, 12, 31)

        assertEquals(
            LocalDate.of(2027, 1, 31),
            SavingsPlanCalculator.occurrenceDate(december31, PlanFrequency.MONTHLY, 1),
        )
        assertEquals(
            LocalDate.of(2027, 2, 28),
            SavingsPlanCalculator.occurrenceDate(december31, PlanFrequency.MONTHLY, 2),
        )
    }

    @Test
    fun dailyAndWeeklyRecurFromToday() {
        assertEquals(
            LocalDate.of(2026, 11, 8),
            SavingsPlanCalculator.occurrenceDate(start, PlanFrequency.DAILY, 2),
        )
        assertEquals(
            LocalDate.of(2026, 11, 20),
            SavingsPlanCalculator.occurrenceDate(start, PlanFrequency.WEEKLY, 2),
        )
    }

    @Test
    fun requiredContributionsRoundUp() {
        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = 1_000,
            availableBalanceIdr = 500,
            contributionIdr = 300,
            frequency = PlanFrequency.WEEKLY,
            startDate = start,
        )

        assertEquals(500L, projection.remainingIdr)
        assertEquals(2L, projection.requiredContributions)
        assertEquals(start.plusDays(7), projection.completionDate)
    }

    @Test
    fun contributionLargerThanRemaining_completesToday() {
        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = 1_000,
            availableBalanceIdr = 900,
            contributionIdr = 5_000,
            frequency = PlanFrequency.MONTHLY,
            startDate = start,
        )

        assertEquals(1L, projection.requiredContributions)
        assertEquals(start, projection.completionDate)
    }

    @Test
    fun invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            SavingsPlanCalculator.project(1_000, 0, 0, PlanFrequency.DAILY, start)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SavingsPlanCalculator.project(1_000, 0, -1, PlanFrequency.DAILY, start)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SavingsPlanCalculator.project(-1, 0, 100, PlanFrequency.DAILY, start)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SavingsPlanCalculator.project(1_000, -1, 100, PlanFrequency.DAILY, start)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SavingsPlanCalculator.occurrenceDate(start, PlanFrequency.DAILY, -1)
        }
    }

    @Test
    fun extremelyDistantProjectionReturnsNullDateInsteadOfCrashing() {
        val projection = SavingsPlanCalculator.project(
            targetPriceIdr = Long.MAX_VALUE,
            availableBalanceIdr = 0,
            contributionIdr = 1,
            frequency = PlanFrequency.DAILY,
            startDate = start,
        )

        assertEquals(Long.MAX_VALUE, projection.requiredContributions)
        assertNull(projection.completionDate)
        assertFalse(projection.isGoalReached)
    }
}
