package id.my.f1zz.whenyah.data.repository

import androidx.room.withTransaction
import id.my.f1zz.whenyah.data.local.WhenYahDatabase
import id.my.f1zz.whenyah.data.local.dao.SavingsEventDao
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
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
    private val database: WhenYahDatabase,
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

        database.withTransaction {
            val balance = SavingsCalculator.balance(savingsEventDao.getByItem(itemId))
            try {
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
        }
    }

    suspend fun recordWithdrawal(
        itemId: UUID,
        amountIdr: Long,
        note: String?,
    ): Result<Unit> = runCatching {
        require(amountIdr > 0) { "Amount must be greater than zero" }

        database.withTransaction {
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
    }

    private fun normalizeNote(note: String?): String? =
        note?.trim()?.takeIf { it.isNotEmpty() }
}
