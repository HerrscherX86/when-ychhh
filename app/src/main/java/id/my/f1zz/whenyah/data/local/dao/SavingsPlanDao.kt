package id.my.f1zz.whenyah.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT * FROM savings_plans WHERE id = :planId LIMIT 1")
    suspend fun getById(planId: UUID): SavingsPlanEntity?

    @Query("SELECT * FROM savings_plans")
    suspend fun getAllPlans(): List<SavingsPlanEntity>

    @Query("SELECT * FROM savings_plans WHERE active = 1")
    fun observeActivePlans(): Flow<List<SavingsPlanEntity>>

    @Upsert
    suspend fun upsert(plan: SavingsPlanEntity)

    @Query("DELETE FROM savings_plans WHERE itemId = :itemId")
    suspend fun deleteByItem(itemId: UUID)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<SavingsPlanEntity>)

    @Query("DELETE FROM savings_plans")
    suspend fun deleteAll()
}
