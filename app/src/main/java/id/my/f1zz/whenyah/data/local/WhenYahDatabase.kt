package id.my.f1zz.whenyah.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.my.f1zz.whenyah.data.local.dao.CollectionDao
import id.my.f1zz.whenyah.data.local.dao.SavingsEventDao
import id.my.f1zz.whenyah.data.local.dao.WishlistItemDao
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.data.local.entity.PurchaseRecordEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity

@Database(
    entities = [
        CollectionEntity::class,
        WishlistItemEntity::class,
        SavingsEventEntity::class,
        SavingsPlanEntity::class,
        PurchaseRecordEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class WhenYahDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun wishlistItemDao(): WishlistItemDao
    abstract fun savingsEventDao(): SavingsEventDao

    companion object {
        const val NAME = "whenyah.db"
    }
}
