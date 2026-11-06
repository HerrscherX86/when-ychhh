package id.my.f1zz.whenyah.data.repository

import androidx.room.withTransaction
import id.my.f1zz.whenyah.data.local.WhenYahDatabase
import id.my.f1zz.whenyah.data.local.dao.PurchaseRecordDao
import id.my.f1zz.whenyah.data.local.dao.SavingsEventDao
import id.my.f1zz.whenyah.data.local.dao.WishlistItemDao
import id.my.f1zz.whenyah.data.local.entity.PurchaseRecordEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.purchase.PurchaseAccounting
import id.my.f1zz.whenyah.domain.savings.SavingsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val database: WhenYahDatabase,
    private val itemDao: WishlistItemDao,
    private val savingsEventDao: SavingsEventDao,
    private val purchaseRecordDao: PurchaseRecordDao,
) {
    fun observePurchasedItemsWithSavings(): Flow<List<WishlistItemWithSavings>> =
        combine(itemDao.observePurchased(), savingsEventDao.observeAll()) { items, events ->
            val eventsByItem = events.groupBy { it.itemId }
            items.map { item ->
                WishlistItemWithSavings(
                    item = item,
                    savings = SavingsCalculator.summarize(
                        entries = eventsByItem[item.id].orEmpty(),
                        targetPriceIdr = item.targetPriceIdr,
                    ),
                )
            }
        }

    fun observePurchaseRecords(): Flow<List<PurchaseRecordEntity>> =
        purchaseRecordDao.observeAll()

    fun observeLatestPurchaseRecord(itemId: UUID): Flow<PurchaseRecordEntity?> =
        purchaseRecordDao.observeLatestForItem(itemId)

    suspend fun purchaseItem(itemId: UUID): Result<PurchaseRecordEntity> = runCatching {
        database.withTransaction {
            val item = itemDao.findById(itemId) ?: error("Item not found")

            if (item.status == ItemStatus.PURCHASED) {
                return@withTransaction purchaseRecordDao.getLatestForItem(itemId)
                    ?: error("Item is already purchased")
            }

            val availableBalance = SavingsCalculator.balance(
                savingsEventDao.getByItem(itemId)
            )
            val spendPlan = PurchaseAccounting.plan(availableBalance)
            val completedAt = Instant.now()
            val purchaseRecord = PurchaseRecordEntity(
                id = UUID.randomUUID(),
                itemId = itemId,
                completedAt = completedAt,
                savingsSpentIdr = spendPlan.savingsSpentIdr,
            )

            purchaseRecordDao.insert(purchaseRecord)

            if (spendPlan.shouldWriteSpendEvent) {
                savingsEventDao.insert(
                    SavingsEventEntity(
                        id = UUID.randomUUID(),
                        itemId = itemId,
                        type = SavingsEventType.PURCHASE_SPEND,
                        amountIdr = spendPlan.savingsSpentIdr,
                        note = null,
                        createdAt = completedAt,
                        purchaseRecordId = purchaseRecord.id,
                    )
                )
            }

            itemDao.upsert(
                item.copy(
                    status = ItemStatus.PURCHASED,
                    purchasedAt = completedAt,
                    updatedAt = completedAt,
                )
            )

            purchaseRecord
        }
    }

    suspend fun restoreItem(itemId: UUID): Result<Unit> = runCatching {
        database.withTransaction {
            val item = itemDao.findById(itemId) ?: error("Item not found")
            if (item.status == ItemStatus.ACTIVE) {
                return@withTransaction
            }

            itemDao.upsert(
                item.copy(
                    status = ItemStatus.ACTIVE,
                    purchasedAt = null,
                    updatedAt = Instant.now(),
                )
            )
        }
    }
}
