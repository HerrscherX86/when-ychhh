package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.repository.WishlistItemWithSavings
import id.my.f1zz.whenyah.data.widget.WidgetSnapshotFactory
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.savings.SavingsSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class WidgetSnapshotFactoryTest {
    @Test
    fun `empty wishlist produces empty snapshot`() {
        val snapshot = WidgetSnapshotFactory.create(emptyList())

        assertTrue(snapshot.isEmpty)
        assertEquals(0, snapshot.activeItemCount)
        assertEquals(0L, snapshot.totalSavedIdr)
        assertEquals(0L, snapshot.totalTargetIdr)
        assertEquals(0f, snapshot.overallProgress)
        assertEquals(0, snapshot.fullyFundedCount)
        assertNull(snapshot.closestGoal)
        assertTrue(snapshot.readyToBuy.isEmpty())
    }

    @Test
    fun `aggregates match dashboard totals`() {
        val snapshot = WidgetSnapshotFactory.create(
            listOf(
                entry(name = "Headphones", target = 1_000, available = 400),
                entry(name = "Keyboard", target = 2_000, available = 500),
            )
        )

        assertEquals(2, snapshot.activeItemCount)
        assertEquals(900L, snapshot.totalSavedIdr)
        assertEquals(3_000L, snapshot.totalTargetIdr)
        assertEquals(0.3f, snapshot.overallProgress)
        assertEquals(0, snapshot.fullyFundedCount)
    }

    @Test
    fun `closest goal uses the smallest remaining amount`() {
        val closest = entry(name = "Headphones", target = 1_000, available = 400)
        val farther = entry(name = "Keyboard", target = 2_000, available = 500)

        val snapshot = WidgetSnapshotFactory.create(listOf(farther, closest))

        assertEquals(closest.item.id, snapshot.closestGoal?.itemId)
        assertEquals(600L, snapshot.closestGoal?.remainingIdr)
    }

    @Test
    fun `closest goal excludes funded and zero-target items`() {
        val funded = entry(name = "Funded", target = 1_000, available = 1_000)
        val free = entry(name = "Free", target = 0, available = 0)
        val saving = entry(name = "Saving", target = 1_000, available = 250)

        val snapshot = WidgetSnapshotFactory.create(listOf(funded, free, saving))

        assertEquals(saving.item.id, snapshot.closestGoal?.itemId)
    }

    @Test
    fun `ready to buy lists funded items excluding zero targets sorted by name`() {
        val beta = entry(name = "Beta", target = 1_000, available = 1_000)
        val alpha = entry(name = "Alpha", target = 500, available = 500)
        val free = entry(name = "Free", target = 0, available = 0)
        val saving = entry(name = "Saving", target = 1_000, available = 250)

        val snapshot = WidgetSnapshotFactory.create(listOf(beta, alpha, free, saving))

        assertEquals(listOf("Alpha", "Beta"), snapshot.readyToBuy.map { it.name })
        assertEquals(2, snapshot.readyToBuy.size)
    }

    @Test
    fun `surplus savings keep progress at one and count as ready`() {
        val snapshot = WidgetSnapshotFactory.create(
            listOf(entry(name = "Overfunded", target = 1_000, available = 1_200))
        )

        assertEquals(1f, snapshot.overallProgress)
        assertEquals(1_200L, snapshot.totalSavedIdr)
        assertEquals(1, snapshot.fullyFundedCount)
        assertEquals(1, snapshot.readyToBuy.size)
        assertEquals(1f, snapshot.readyToBuy.single().progress)
        assertEquals(0L, snapshot.readyToBuy.single().remainingIdr)
    }

    private fun entry(
        name: String,
        target: Long,
        available: Long,
        id: UUID = UUID.randomUUID(),
    ): WishlistItemWithSavings {
        val remaining = (target - available).coerceAtLeast(0L)
        val surplus = (available - target).coerceAtLeast(0L)
        val progress = if (target == 0L) {
            1f
        } else {
            (available.toDouble() / target.toDouble()).coerceAtMost(1.0).toFloat()
        }
        return WishlistItemWithSavings(
            item = WishlistItemEntity(
                id = id,
                name = name,
                targetPriceIdr = target,
                imageRelativePath = null,
                purchaseUrl = null,
                notes = null,
                category = null,
                collectionId = null,
                status = ItemStatus.ACTIVE,
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                purchasedAt = null,
            ),
            savings = SavingsSummary(
                availableIdr = available,
                targetIdr = target,
                remainingIdr = remaining,
                surplusIdr = surplus,
                progress = progress,
                isGoalReached = available >= target,
            ),
        )
    }
}
