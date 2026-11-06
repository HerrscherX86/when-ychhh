package id.my.f1zz.whenyah.domain.notifications

import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object NotificationScheduleCalculator {
    /**
     * Returns the first occurrence at [time] that is after [now]. Occurrences
     * advance from [anchorDate]; monthly schedules stay anchored to the start
     * day-of-month and clamp at month end.
     */
    fun nextOccurrence(
        now: ZonedDateTime,
        frequency: PlanFrequency,
        time: LocalTime,
        anchorDate: LocalDate = now.toLocalDate(),
    ): ZonedDateTime {
        var index = 0L
        while (true) {
            val date = when (frequency) {
                PlanFrequency.DAILY -> anchorDate.plusDays(index)
                PlanFrequency.WEEKLY -> anchorDate.plusWeeks(index)
                PlanFrequency.MONTHLY -> anchorDate.plusMonths(index)
            }
            val candidate = date.atTime(time).atZone(now.zone)
            if (candidate.isAfter(now)) {
                return candidate
            }
            index++
        }
    }

    fun nextSummary(
        now: ZonedDateTime,
        cadence: SummaryCadence,
        time: LocalTime,
        anchorDate: LocalDate = now.toLocalDate(),
    ): ZonedDateTime? = when (cadence) {
        SummaryCadence.OFF -> null
        SummaryCadence.WEEKLY -> nextOccurrence(
            now = now,
            frequency = PlanFrequency.WEEKLY,
            time = time,
            anchorDate = anchorDate,
        )

        SummaryCadence.MONTHLY -> nextOccurrence(
            now = now,
            frequency = PlanFrequency.MONTHLY,
            time = time,
            anchorDate = anchorDate,
        )
    }
}
