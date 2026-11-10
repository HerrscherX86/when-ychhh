package id.my.f1zz.whenyah.domain.wishlist

import java.time.Instant
import java.util.UUID

enum class ItemSortOrder {
    RECENT,
    NAME,
    PROGRESS,
    TARGET_PRICE,
}

enum class GoalFilter {
    ALL,
    REACHED,
    NOT_REACHED,
}

enum class PlanFilter {
    ALL,
    HAS_PLAN,
    NO_PLAN,
}

data class WishlistItemFacts(
    val id: UUID,
    val name: String,
    val updatedAt: Instant,
    val targetPriceIdr: Long,
    val availableIdr: Long,
    val progress: Float,
    val isGoalReached: Boolean,
    val collectionId: UUID?,
    val category: String?,
    val hasPlan: Boolean,
)

data class WishlistQuery(
    val text: String = "",
    val sort: ItemSortOrder = ItemSortOrder.RECENT,
    val collectionId: UUID? = null,
    val category: String? = null,
    val goal: GoalFilter = GoalFilter.ALL,
    val plan: PlanFilter = PlanFilter.ALL,
) {
    val isFiltering: Boolean
        get() = collectionId != null ||
            category != null ||
            goal != GoalFilter.ALL ||
            plan != PlanFilter.ALL
}

object WishlistQueryEngine {
    fun applyQuery(
        items: List<WishlistItemFacts>,
        query: WishlistQuery,
    ): List<WishlistItemFacts> {
        val filtered = items.filter { item ->
            item.matchesText(query.text) &&
                item.matchesCollection(query.collectionId) &&
                item.matchesCategory(query.category) &&
                item.matchesGoal(query.goal) &&
                item.matchesPlan(query.plan)
        }

        return when (query.sort) {
            ItemSortOrder.RECENT -> filtered.sortedWith(
                compareByDescending<WishlistItemFacts> { it.updatedAt }.thenBy { it.id }
            )

            ItemSortOrder.NAME -> filtered.sortedWith(
                compareBy<WishlistItemFacts> { it.name.lowercase() }.thenBy { it.id }
            )

            ItemSortOrder.PROGRESS -> filtered.sortedWith(
                compareByDescending<WishlistItemFacts> { it.progress }.thenBy { it.id }
            )

            ItemSortOrder.TARGET_PRICE -> filtered.sortedWith(
                compareBy<WishlistItemFacts> { it.targetPriceIdr }.thenBy { it.id }
            )
        }
    }

    private fun WishlistItemFacts.matchesText(text: String): Boolean =
        text.isBlank() || name.contains(text, ignoreCase = true)

    private fun WishlistItemFacts.matchesCollection(collectionId: UUID?): Boolean =
        collectionId == null || this.collectionId == collectionId

    private fun WishlistItemFacts.matchesCategory(category: String?): Boolean =
        category == null || this.category.equals(category, ignoreCase = true)

    private fun WishlistItemFacts.matchesGoal(goal: GoalFilter): Boolean = when (goal) {
        GoalFilter.ALL -> true
        GoalFilter.REACHED -> isGoalReached
        GoalFilter.NOT_REACHED -> !isGoalReached
    }

    private fun WishlistItemFacts.matchesPlan(plan: PlanFilter): Boolean = when (plan) {
        PlanFilter.ALL -> true
        PlanFilter.HAS_PLAN -> hasPlan
        PlanFilter.NO_PLAN -> !hasPlan
    }
}
