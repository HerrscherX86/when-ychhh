package id.my.f1zz.whenyah.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "purchase_records",
    foreignKeys = [
        ForeignKey(
            entity = WishlistItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemId")],
)
data class PurchaseRecordEntity(
    @PrimaryKey val id: UUID,
    val itemId: UUID,
    val completedAt: Instant,
    val savingsSpentIdr: Long,
)
