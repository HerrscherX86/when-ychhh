package id.my.f1zz.whenyah.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.my.f1zz.whenyah.domain.model.ItemStatus
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "wishlist_items",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("collectionId"),
        Index("status"),
    ],
)
data class WishlistItemEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val targetPriceIdr: Long,
    val imageRelativePath: String?,
    val purchaseUrl: String?,
    val notes: String?,
    val category: String?,
    val collectionId: UUID?,
    val status: ItemStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val purchasedAt: Instant?,
)
