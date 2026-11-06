package id.my.f1zz.whenyah.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import id.my.f1zz.whenyah.data.local.entity.PurchaseRecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PurchaseRecordDao {
    @Insert
    suspend fun insert(record: PurchaseRecordEntity)

    @Query("SELECT * FROM purchase_records ORDER BY completedAt DESC, id DESC")
    fun observeAll(): Flow<List<PurchaseRecordEntity>>

    @Query(
        """
        SELECT * FROM purchase_records
        WHERE itemId = :itemId
        ORDER BY completedAt DESC, id DESC
        LIMIT 1
        """
    )
    fun observeLatestForItem(itemId: UUID): Flow<PurchaseRecordEntity?>

    @Query(
        """
        SELECT * FROM purchase_records
        WHERE itemId = :itemId
        ORDER BY completedAt DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getLatestForItem(itemId: UUID): PurchaseRecordEntity?
}
