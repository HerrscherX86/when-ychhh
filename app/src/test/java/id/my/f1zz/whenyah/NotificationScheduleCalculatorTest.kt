package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.notifications.NotificationScheduleCalculator
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NotificationScheduleCalculatorTest {
    private val zone = ZoneId.of("Asia/Jakarta")
    private val nineAm = LocalTime.of(9, 0)

    @Test
    fun dailyReminderUsesTodayWhenTimeHasNotPassed() {
        val now = ZonedDateTime.of(2026, 11, 6, 8, 0, 0, 0, zone)

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.DAILY,
            time = nineAm,
        )

        assertEquals(ZonedDateTime.of(2026, 11, 6, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun dailyReminderMovesToTomorrowWhenTimeHasPassed() {
        val now = ZonedDateTime.of(2026, 11, 6, 10, 0, 0, 0, zone)

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.DAILY,
            time = nineAm,
        )

        assertEquals(ZonedDateTime.of(2026, 11, 7, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun weeklyReminderMovesSevenDaysForward() {
        val now = ZonedDateTime.of(2026, 11, 6, 10, 0, 0, 0, zone)

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.WEEKLY,
            time = nineAm,
        )

        assertEquals(ZonedDateTime.of(2026, 11, 13, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun monthlyReminderClampsToFebruaryEnd() {
        val now = ZonedDateTime.of(2026, 1, 31, 10, 0, 0, 0, zone)

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.MONTHLY,
            time = nineAm,
        )

        assertEquals(ZonedDateTime.of(2026, 2, 28, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun monthlyReminderClampsToLeapDay() {
        val now = ZonedDateTime.of(2028, 1, 31, 10, 0, 0, 0, zone)

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.MONTHLY,
            time = nineAm,
        )

        assertEquals(ZonedDateTime.of(2028, 2, 29, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun monthlyRescheduleKeepsOriginalAnchorDay() {
        val now = ZonedDateTime.of(2026, 3, 1, 10, 0, 0, 0, zone)

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.MONTHLY,
            time = nineAm,
            anchorDate = LocalDate.of(2026, 1, 31),
        )

        assertEquals(ZonedDateTime.of(2026, 3, 31, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun summaryCadenceOffHasNoSchedule() {
        val now = ZonedDateTime.of(2026, 11, 6, 8, 0, 0, 0, zone)

        assertNull(
            NotificationScheduleCalculator.nextSummary(
                now = now,
                cadence = SummaryCadence.OFF,
                time = nineAm,
            )
        )
    }

    @Test
    fun weeklyReminderKeepsItsAnchorDayInsteadOfToday() {
        val now = ZonedDateTime.of(2026, 11, 4, 8, 0, 0, 0, zone) // Wednesday

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.WEEKLY,
            time = nineAm,
            anchorDate = LocalDate.of(2026, 11, 2), // Monday
        )

        assertEquals(ZonedDateTime.of(2026, 11, 9, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun monthlyReminderKeepsItsAnchorDayInsteadOfToday() {
        val now = ZonedDateTime.of(2026, 11, 4, 8, 0, 0, 0, zone)

        val next = NotificationScheduleCalculator.nextOccurrence(
            now = now,
            frequency = PlanFrequency.MONTHLY,
            time = nineAm,
            anchorDate = LocalDate.of(2026, 10, 15),
        )

        assertEquals(ZonedDateTime.of(2026, 11, 15, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun summaryKeepsItsAnchorDayInsteadOfToday() {
        val now = ZonedDateTime.of(2026, 11, 4, 8, 0, 0, 0, zone) // Wednesday

        val next = NotificationScheduleCalculator.nextSummary(
            now = now,
            cadence = SummaryCadence.WEEKLY,
            time = nineAm,
            anchorDate = LocalDate.of(2026, 11, 2), // Monday
        )

        assertEquals(ZonedDateTime.of(2026, 11, 9, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun summaryCadenceUsesWeeklyOrMonthlyCalendarRecurrence() {
        val now = ZonedDateTime.of(2026, 11, 6, 10, 0, 0, 0, zone)

        assertEquals(
            ZonedDateTime.of(2026, 11, 13, 9, 0, 0, 0, zone),
            NotificationScheduleCalculator.nextSummary(
                now = now,
                cadence = SummaryCadence.WEEKLY,
                time = nineAm,
            ),
        )
        assertEquals(
            ZonedDateTime.of(2026, 12, 6, 9, 0, 0, 0, zone),
            NotificationScheduleCalculator.nextSummary(
                now = now,
                cadence = SummaryCadence.MONTHLY,
                time = nineAm,
            ),
        )
    }
}
