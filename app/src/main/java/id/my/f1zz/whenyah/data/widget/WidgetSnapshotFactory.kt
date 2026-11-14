package id.my.f1zz.whenyah.data.widget

import id.my.f1zz.whenyah.data.repository.WishlistItemWithSavings
import id.my.f1zz.whenyah.domain.dashboard.DashboardCalculator
import id.my.f1zz.whenyah.domain.dashboard.DashboardItem

object WidgetSnapshotFactory {
    fun create(items: List<WishlistItemWithSavings>): WidgetSnapshot {
        val summary = DashboardCalculator.summarize(
            items.map { entry ->
                DashboardItem(
                    targetPriceIdr = entry.item.targetPriceIdr,
                    availableIdr = entry.savings.availableIdr,
                )
            }
        )

        val closestGoal = items
            .asSequence()
            .filter { entry ->
                entry.item.targetPriceIdr > 0 && !entry.savings.isGoalReached
            }
            .minWithOrNull(
                compareBy<WishlistItemWithSavings>(
                    { it.savings.remainingIdr },
                    { it.item.name },
                    { it.item.id.toString() },
                )
            )
            ?.toWidgetGoal()

        val readyToBuy = items
            .asSequence()
            .filter { entry ->
                entry.item.targetPriceIdr > 0 && entry.savings.isGoalReached
            }
            .sortedWith(
                compareBy<WishlistItemWithSavings>(
                    { it.item.name },
                    { it.item.id.toString() },
                )
            )
            .map { it.toWidgetGoal() }
            .toList()

        return WidgetSnapshot(
            activeItemCount = summary.activeItemCount,
            totalSavedIdr = summary.totalAvailableIdr,
            totalTargetIdr = summary.totalTargetIdr,
            overallProgress = summary.overallProgress,
            fullyFundedCount = summary.fullyFundedCount,
            closestGoal = closestGoal,
            readyToBuy = readyToBuy,
        )
    }

    private fun WishlistItemWithSavings.toWidgetGoal(): WidgetGoal = WidgetGoal(
        itemId = item.id,
        name = item.name,
        savedIdr = savings.availableIdr,
        targetIdr = item.targetPriceIdr,
        remainingIdr = savings.remainingIdr,
        progress = savings.progress,
    )
}
