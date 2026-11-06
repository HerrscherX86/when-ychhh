package id.my.f1zz.whenyah.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SavingsEventDao {
    @Query(
        """
        SELECT * FROM savings_events
        WHERE itemId = :itemId
        ORDER BY createdAt DESC, id DESC
        """
    )
    fun observeByItem(itemId: UUID): Flow<List<SavingsEventEntity>>

    @Query("SELECT * FROM savings_events ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<SavingsEventEntity>>

    @Query(
        """
        SELECT * FROM savings_events
        WHERE itemId = :itemId
        ORDER BY createdAt ASC, id ASC
        """
    )
    suspend fun getByItem(itemId: UUID): List<SavingsEventEntity>

    @Insert
    suspend fun insert(event: SavingsEventEntity)
}
