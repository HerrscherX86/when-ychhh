package id.my.f1zz.whenyah.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "savings_events",
    foreignKeys = [
        ForeignKey(
            entity = WishlistItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("itemId"),
        Index("purchaseRecordId"),
    ],
)
data class SavingsEventEntity(
    @PrimaryKey val id: UUID,
    val itemId: UUID,
    val type: SavingsEventType,
    val amountIdr: Long,
    val note: String?,
    val createdAt: Instant,
    val purchaseRecordId: UUID?,
)
