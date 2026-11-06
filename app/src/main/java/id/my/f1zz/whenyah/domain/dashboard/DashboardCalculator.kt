package id.my.f1zz.whenyah.domain.dashboard

data class DashboardItem(
    val targetPriceIdr: Long,
    val availableIdr: Long,
)

data class DashboardSummary(
    val activeItemCount: Int,
    val totalTargetIdr: Long,
    val totalAvailableIdr: Long,
    val overallProgress: Float,
    val fullyFundedCount: Int,
) {
    companion object {
        val Empty = DashboardSummary(
            activeItemCount = 0,
            totalTargetIdr = 0,
            totalAvailableIdr = 0,
            overallProgress = 0f,
            fullyFundedCount = 0,
        )
    }
}

object DashboardCalculator {
    fun summarize(items: List<DashboardItem>): DashboardSummary {
        var totalTarget = 0L
        var totalAvailable = 0L
        var fullyFunded = 0

        items.forEach { item ->
            require(item.targetPriceIdr >= 0) { "Target price cannot be negative" }
            require(item.availableIdr >= 0) { "Available balance cannot be negative" }

            totalTarget = Math.addExact(totalTarget, item.targetPriceIdr)
            totalAvailable = Math.addExact(totalAvailable, item.availableIdr)
            if (item.availableIdr >= item.targetPriceIdr) {
                fullyFunded++
            }
        }

        val progress = when {
            items.isEmpty() -> 0f
            totalTarget == 0L -> 1f
            else -> (totalAvailable.toDouble() / totalTarget.toDouble())
                .coerceAtMost(1.0)
                .toFloat()
        }

        return DashboardSummary(
            activeItemCount = items.size,
            totalTargetIdr = totalTarget,
            totalAvailableIdr = totalAvailable,
            overallProgress = progress,
            fullyFundedCount = fullyFunded,
        )
    }
}
