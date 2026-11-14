package id.my.f1zz.whenyah.data.repository

import androidx.room.withTransaction
import id.my.f1zz.whenyah.data.local.WhenYahDatabase
import id.my.f1zz.whenyah.data.local.dao.SavingsEventDao
import id.my.f1zz.whenyah.data.local.dao.WishlistItemDao
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.notification.GoalNotifier
import id.my.f1zz.whenyah.data.widget.WidgetUpdater
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.savings.SavingsCalculator
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsRepository @Inject constructor(
    private val savingsEventDao: SavingsEventDao,
    private val itemDao: WishlistItemDao,
    private val database: WhenYahDatabase,
    private val goalNotifier: GoalNotifier,
    private val widgetUpdater: WidgetUpdater,
) {
    fun observeEvents(itemId: UUID): Flow<List<SavingsEventEntity>> =
        savingsEventDao.observeByItem(itemId)

    fun observeAllEvents(): Flow<List<SavingsEventEntity>> = savingsEventDao.observeAll()

    suspend fun getBalance(itemId: UUID): Long =
        SavingsCalculator.balance(savingsEventDao.getByItem(itemId))

    suspend fun recordDeposit(
        itemId: UUID,
        amountIdr: Long,
        note: String?,
    ): Result<Unit> = runCatching {
        require(amountIdr > 0) { "Amount must be greater than zero" }

        val goalPayload = database.withTransaction {
            val item = itemDao.findById(itemId) ?: error("Item not found")
            require(item.status == ItemStatus.ACTIVE) { "Item is not active" }

            val balance = SavingsCalculator.balance(savingsEventDao.getByItem(itemId))
            val newBalance = try {
                Math.addExact(balance, amountIdr)
            } catch (_: ArithmeticException) {
                throw IllegalArgumentException("Amount is too large")
            }

            savingsEventDao.insert(
                SavingsEventEntity(
                    id = UUID.randomUUID(),
                    itemId = itemId,
                    type = SavingsEventType.DEPOSIT,
                    amountIdr = amountIdr,
                    note = normalizeNote(note),
                    createdAt = Instant.now(),
                    purchaseRecordId = null,
                )
            )

            if (item.targetPriceIdr > balance && newBalance >= item.targetPriceIdr) {
                GoalReachedPayload(
                    itemId = item.id,
                    itemName = item.name,
                    targetIdr = item.targetPriceIdr,
                    savedIdr = newBalance,
                )
            } else {
                null
            }
        }

        goalPayload?.let { payload ->
            runCatching {
                goalNotifier.notifyGoalReached(
                    itemId = payload.itemId,
                    itemName = payload.itemName,
                    targetIdr = payload.targetIdr,
                    savedIdr = payload.savedIdr,
                )
            }
        }

        widgetUpdater.refresh()
    }

    suspend fun recordWithdrawal(
        itemId: UUID,
        amountIdr: Long,
        note: String?,
    ): Result<Unit> = runCatching {
        require(amountIdr > 0) { "Amount must be greater than zero" }

        database.withTransaction {
            val item = itemDao.findById(itemId) ?: error("Item not found")
            require(item.status == ItemStatus.ACTIVE) { "Item is not active" }

            val balance = SavingsCalculator.balance(savingsEventDao.getByItem(itemId))
            require(amountIdr <= balance) { "Withdrawal exceeds available savings" }

            savingsEventDao.insert(
                SavingsEventEntity(
                    id = UUID.randomUUID(),
                    itemId = itemId,
                    type = SavingsEventType.WITHDRAWAL,
                    amountIdr = amountIdr,
                    note = normalizeNote(note),
                    createdAt = Instant.now(),
                    purchaseRecordId = null,
                )
            )
        }

        widgetUpdater.refresh()
    }

    private fun normalizeNote(note: String?): String? =
        note?.trim()?.takeIf { it.isNotEmpty() }

    private data class GoalReachedPayload(
        val itemId: UUID,
        val itemName: String,
        val targetIdr: Long,
        val savedIdr: Long,
    )
}
