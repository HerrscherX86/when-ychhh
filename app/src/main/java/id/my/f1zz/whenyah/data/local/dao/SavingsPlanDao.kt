package id.my.f1zz.whenyah.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SavingsPlanDao {
    @Query("SELECT * FROM savings_plans WHERE itemId = :itemId LIMIT 1")
    fun observeByItem(itemId: UUID): Flow<SavingsPlanEntity?>

    @Query("SELECT * FROM savings_plans WHERE itemId = :itemId LIMIT 1")
    suspend fun getByItem(itemId: UUID): SavingsPlanEntity?

    @Upsert
    suspend fun upsert(plan: SavingsPlanEntity)

    @Query("DELETE FROM savings_plans WHERE itemId = :itemId")
    suspend fun deleteByItem(itemId: UUID)
}
