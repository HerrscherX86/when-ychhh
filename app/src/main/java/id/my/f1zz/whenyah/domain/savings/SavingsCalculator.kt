package id.my.f1zz.whenyah.domain.savings

import id.my.f1zz.whenyah.domain.model.SavingsEventType

object SavingsCalculator {
    fun balance(entries: List<SavingsLedgerEntry>): Long {
        var total = 0L
        entries.forEach { entry ->
            total = when (entry.type) {
                SavingsEventType.DEPOSIT -> Math.addExact(total, entry.amountIdr)
                SavingsEventType.WITHDRAWAL,
                SavingsEventType.PURCHASE_SPEND,
                -> Math.subtractExact(total, entry.amountIdr)
            }
        }
        return total
    }

    fun summarize(
        entries: List<SavingsLedgerEntry>,
        targetPriceIdr: Long,
    ): SavingsSummary {
        require(targetPriceIdr >= 0) { "Target price cannot be negative" }

        val available = balance(entries).coerceAtLeast(0)
        val isGoalReached = available >= targetPriceIdr
        val remaining = if (isGoalReached) 0 else targetPriceIdr - available
        val surplus = if (available > targetPriceIdr) available - targetPriceIdr else 0
        val progress = if (targetPriceIdr == 0L) {
            1f
        } else {
            (available.toDouble() / targetPriceIdr.toDouble())
                .coerceAtMost(1.0)
                .toFloat()
        }

        return SavingsSummary(
            availableIdr = available,
            targetIdr = targetPriceIdr,
            remainingIdr = remaining,
            surplusIdr = surplus,
            progress = progress,
            isGoalReached = isGoalReached,
        )
    }

    fun canWithdraw(entries: List<SavingsLedgerEntry>, amountIdr: Long): Boolean =
        amountIdr > 0 && amountIdr <= balance(entries)
}
