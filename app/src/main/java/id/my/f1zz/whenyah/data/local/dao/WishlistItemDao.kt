package id.my.f1zz.whenyah.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface WishlistItemDao {
    @Query("SELECT * FROM wishlist_items WHERE status = 'ACTIVE' ORDER BY updatedAt DESC")
    fun observeActive(): Flow<List<WishlistItemEntity>>

    @Query(
        """
        SELECT * FROM wishlist_items
        WHERE status = 'ACTIVE' AND collectionId = :collectionId
        ORDER BY updatedAt DESC
        """
    )
    fun observeActiveByCollection(collectionId: UUID): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE id = :id LIMIT 1")
    fun observeById(id: UUID): Flow<WishlistItemEntity?>

    @Query("SELECT * FROM wishlist_items WHERE id = :id LIMIT 1")
    suspend fun findById(id: UUID): WishlistItemEntity?

    @Upsert
    suspend fun upsert(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteById(id: UUID)
}
