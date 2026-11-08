package id.my.f1zz.whenyah.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity(
    tableName = "savings_plans",
    foreignKeys = [
        ForeignKey(
            entity = WishlistItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["itemId"], unique = true)],
)
data class SavingsPlanEntity(
    @PrimaryKey val id: UUID,
    val itemId: UUID,
    val contributionIdr: Long,
    val frequency: PlanFrequency,
    val reminderEnabled: Boolean,
    val reminderLocalTime: LocalTime?,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val reminderAnchorDate: LocalDate? = null,
)
