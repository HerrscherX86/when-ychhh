package id.my.f1zz.whenyah.data.local.model

import androidx.room.Embedded
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity

data class CollectionWithItemCount(
    @Embedded val collection: CollectionEntity,
    val itemCount: Int,
)
