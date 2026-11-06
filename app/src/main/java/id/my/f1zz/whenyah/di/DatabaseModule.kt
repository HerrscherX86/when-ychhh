package id.my.f1zz.whenyah.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.my.f1zz.whenyah.data.local.WhenYahDatabase
import id.my.f1zz.whenyah.data.local.dao.CollectionDao
import id.my.f1zz.whenyah.data.local.dao.SavingsEventDao
import id.my.f1zz.whenyah.data.local.dao.WishlistItemDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WhenYahDatabase =
        Room.databaseBuilder(context, WhenYahDatabase::class.java, WhenYahDatabase.NAME)
            .build()

    @Provides
    fun provideCollectionDao(database: WhenYahDatabase): CollectionDao = database.collectionDao()

    @Provides
    fun provideWishlistItemDao(database: WhenYahDatabase): WishlistItemDao =
        database.wishlistItemDao()

    @Provides
    fun provideSavingsEventDao(database: WhenYahDatabase): SavingsEventDao =
        database.savingsEventDao()
}
