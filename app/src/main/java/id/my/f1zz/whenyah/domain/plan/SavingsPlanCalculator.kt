package id.my.f1zz.whenyah.domain.plan

import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.time.LocalDate

object SavingsPlanCalculator {
    /**
     * Projects completion from today's local date. Planned contributions never
     * modify the ledger; [availableBalanceIdr] is the actual derived balance.
     */
    fun project(
        targetPriceIdr: Long,
        availableBalanceIdr: Long,
        contributionIdr: Long,
        frequency: PlanFrequency,
        startDate: LocalDate,
    ): SavingsPlanProjection {
        require(targetPriceIdr >= 0) { "Target price cannot be negative" }
        require(availableBalanceIdr >= 0) { "Available balance cannot be negative" }
        require(contributionIdr > 0) { "Contribution must be greater than zero" }

        val remaining = if (availableBalanceIdr >= targetPriceIdr) {
            0L
        } else {
            targetPriceIdr - availableBalanceIdr
        }

        if (remaining == 0L) {
            return SavingsPlanProjection(
                remainingIdr = 0,
                requiredContributions = 0,
                completionDate = startDate,
                isGoalReached = true,
            )
        }

        val wholeContributions = remaining / contributionIdr
        val requiredContributions = if (remaining % contributionIdr == 0L) {
            wholeContributions
        } else {
            Math.addExact(wholeContributions, 1)
        }

        val completionDate = runCatching {
            occurrenceDate(startDate, frequency, requiredContributions - 1)
        }.getOrNull()

        return SavingsPlanProjection(
            remainingIdr = remaining,
            requiredContributions = requiredContributions,
            completionDate = completionDate,
            isGoalReached = false,
        )
    }

    /**
     * Occurrence zero is [startDate]. Monthly occurrences stay anchored to the
     * start day-of-month and clamp to the destination month's last valid day.
     */
    fun occurrenceDate(
        startDate: LocalDate,
        frequency: PlanFrequency,
        occurrenceIndex: Long,
    ): LocalDate {
        require(occurrenceIndex >= 0) { "Occurrence index cannot be negative" }

        return when (frequency) {
            PlanFrequency.DAILY -> startDate.plusDays(occurrenceIndex)
            PlanFrequency.WEEKLY -> startDate.plusWeeks(occurrenceIndex)
            PlanFrequency.MONTHLY -> startDate.plusMonths(occurrenceIndex)
        }
    }
}
