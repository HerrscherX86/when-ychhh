package id.my.f1zz.whenyah.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
