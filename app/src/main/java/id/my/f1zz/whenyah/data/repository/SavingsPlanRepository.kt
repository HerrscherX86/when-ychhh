package id.my.f1zz.whenyah.data.repository

import id.my.f1zz.whenyah.data.local.dao.SavingsPlanDao
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsPlanRepository @Inject constructor(
    private val savingsPlanDao: SavingsPlanDao,
) {
    fun observePlan(itemId: UUID): Flow<SavingsPlanEntity?> =
        savingsPlanDao.observeByItem(itemId)

    suspend fun getPlan(itemId: UUID): SavingsPlanEntity? =
        savingsPlanDao.getByItem(itemId)

    suspend fun savePlan(
        itemId: UUID,
        contributionIdr: Long,
        frequency: PlanFrequency,
    ): UUID {
        require(contributionIdr > 0) { "Contribution must be greater than zero" }

        val now = Instant.now()
        val existing = savingsPlanDao.getByItem(itemId)
        val plan = existing?.copy(
            contributionIdr = contributionIdr,
            frequency = frequency,
            updatedAt = now,
        ) ?: SavingsPlanEntity(
            id = UUID.randomUUID(),
            itemId = itemId,
            contributionIdr = contributionIdr,
            frequency = frequency,
            reminderEnabled = false,
            reminderLocalTime = null,
            active = true,
            createdAt = now,
            updatedAt = now,
        )

        savingsPlanDao.upsert(plan)
        return plan.id
    }

    suspend fun setActive(itemId: UUID, active: Boolean) {
        val existing = savingsPlanDao.getByItem(itemId) ?: return
        savingsPlanDao.upsert(
            existing.copy(
                active = active,
                updatedAt = Instant.now(),
            )
        )
    }

    suspend fun deletePlan(itemId: UUID) {
        savingsPlanDao.deleteByItem(itemId)
    }
}
