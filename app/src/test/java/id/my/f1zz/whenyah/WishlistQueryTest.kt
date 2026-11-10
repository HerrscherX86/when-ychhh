package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.wishlist.GoalFilter
import id.my.f1zz.whenyah.domain.wishlist.ItemSortOrder
import id.my.f1zz.whenyah.domain.wishlist.PlanFilter
import id.my.f1zz.whenyah.domain.wishlist.WishlistItemFacts
import id.my.f1zz.whenyah.domain.wishlist.WishlistQuery
import id.my.f1zz.whenyah.domain.wishlist.WishlistQueryEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class WishlistQueryTest {
    private val collectionA = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val collectionB = UUID.fromString("22222222-2222-2222-2222-222222222222")

    private val keyboard = item(
        id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
        name = "Keyboard",
        targetPriceIdr = 1_500_000,
        availableIdr = 500_000,
        progress = 0.3333f,
        isGoalReached = false,
        collectionId = collectionA,
        category = "Electronics",
        hasPlan = true,
        updatedAt = Instant.parse("2026-11-01T00:00:00Z"),
    )
    private val chair = item(
        id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
        name = "Chair",
        targetPriceIdr = 900_000,
        availableIdr = 900_000,
        progress = 1f,
        isGoalReached = true,
        collectionId = collectionB,
        category = "Furniture",
        hasPlan = false,
        updatedAt = Instant.parse("2026-11-03T00:00:00Z"),
    )
    private val manga = item(
        id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
        name = "Manga set",
        targetPriceIdr = 300_000,
        availableIdr = 150_000,
        progress = 0.5f,
        isGoalReached = false,
        collectionId = null,
        category = "electronics",
        hasPlan = false,
        updatedAt = Instant.parse("2026-11-02T00:00:00Z"),
    )
    private val items = listOf(keyboard, chair, manga)

    @Test
    fun noFilters_preservesAllItems() {
        val result = WishlistQueryEngine.applyQuery(items, WishlistQuery())

        assertEquals(3, result.size)
        assertEquals(listOf(chair.id, manga.id, keyboard.id), result.map { it.id })
    }

    @Test
    fun textFilter_matchesNameCaseInsensitively() {
        val result = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(text = "KEY"),
        )

        assertEquals(listOf(keyboard.id), result.map { it.id })
    }

    @Test
    fun collectionFilter_keepsOnlyThatCollection() {
        val result = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(collectionId = collectionB),
        )

        assertEquals(listOf(chair.id), result.map { it.id })
    }

    @Test
    fun categoryFilter_isCaseInsensitiveExactMatch() {
        val result = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(category = "ELECTRONICS"),
        )

        assertEquals(listOf(manga.id, keyboard.id), result.map { it.id })
    }

    @Test
    fun goalFilter_splitsReachedAndNotReached() {
        val reached = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(goal = GoalFilter.REACHED),
        )
        val notReached = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(goal = GoalFilter.NOT_REACHED),
        )

        assertEquals(listOf(chair.id), reached.map { it.id })
        assertEquals(listOf(manga.id, keyboard.id), notReached.map { it.id })
    }

    @Test
    fun planFilter_splitsHasPlanAndNoPlan() {
        val hasPlan = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(plan = PlanFilter.HAS_PLAN),
        )
        val noPlan = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(plan = PlanFilter.NO_PLAN),
        )

        assertEquals(listOf(keyboard.id), hasPlan.map { it.id })
        assertEquals(listOf(chair.id, manga.id), noPlan.map { it.id })
    }

    @Test
    fun sortProgress_ordersByProgressDescending() {
        val result = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(sort = ItemSortOrder.PROGRESS),
        )

        assertEquals(listOf(chair.id, manga.id, keyboard.id), result.map { it.id })
    }

    @Test
    fun sortTargetPrice_ordersCheapestFirst() {
        val result = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(sort = ItemSortOrder.TARGET_PRICE),
        )

        assertEquals(listOf(manga.id, chair.id, keyboard.id), result.map { it.id })
    }

    @Test
    fun sortIsStableForEqualKeys() {
        val first = item(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            name = "Same",
            targetPriceIdr = 1_000,
            availableIdr = 0,
            progress = 0f,
            isGoalReached = false,
        )
        val second = item(
            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
            name = "Same",
            targetPriceIdr = 1_000,
            availableIdr = 0,
            progress = 0f,
            isGoalReached = false,
        )

        val forward = WishlistQueryEngine.applyQuery(
            listOf(second, first),
            WishlistQuery(sort = ItemSortOrder.NAME),
        )
        val reversed = WishlistQueryEngine.applyQuery(
            listOf(first, second),
            WishlistQuery(sort = ItemSortOrder.NAME),
        )

        assertEquals(listOf(first.id, second.id), forward.map { it.id })
        assertEquals(listOf(first.id, second.id), reversed.map { it.id })
    }

    @Test
    fun combinedFilters_areAnded() {
        val result = WishlistQueryEngine.applyQuery(
            items,
            WishlistQuery(
                collectionId = collectionA,
                category = "electronics",
                goal = GoalFilter.NOT_REACHED,
                plan = PlanFilter.HAS_PLAN,
            ),
        )

        assertEquals(listOf(keyboard.id), result.map { it.id })
    }

    @Test
    fun isFiltering_isFalseForBlankTextOnly() {
        assertFalse(WishlistQuery(text = "   ").isFiltering)
        assertFalse(WishlistQuery().isFiltering)
        assertTrue(WishlistQuery(collectionId = collectionA).isFiltering)
        assertTrue(WishlistQuery(category = "Furniture").isFiltering)
        assertTrue(WishlistQuery(goal = GoalFilter.REACHED).isFiltering)
        assertTrue(WishlistQuery(plan = PlanFilter.HAS_PLAN).isFiltering)
    }

    private fun item(
        id: UUID,
        name: String,
        targetPriceIdr: Long,
        availableIdr: Long,
        progress: Float,
        isGoalReached: Boolean,
        collectionId: UUID? = null,
        category: String? = null,
        hasPlan: Boolean = false,
        updatedAt: Instant = Instant.EPOCH,
    ) = WishlistItemFacts(
        id = id,
        name = name,
        updatedAt = updatedAt,
        targetPriceIdr = targetPriceIdr,
        availableIdr = availableIdr,
        progress = progress,
        isGoalReached = isGoalReached,
        collectionId = collectionId,
        category = category,
        hasPlan = hasPlan,
    )
}
