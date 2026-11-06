package id.my.f1zz.whenyah.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.data.local.model.CollectionWithItemCount
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query(
        """
        SELECT collections.*, COUNT(wishlist_items.id) AS itemCount
        FROM collections
        LEFT JOIN wishlist_items
            ON wishlist_items.collectionId = collections.id
            AND wishlist_items.status = 'ACTIVE'
        GROUP BY collections.id
        ORDER BY collections.name COLLATE NOCASE ASC
        """
    )
    fun observeAllWithItemCount(): Flow<List<CollectionWithItemCount>>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    fun observeById(id: UUID): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun findById(id: UUID): CollectionEntity?

    @Upsert
    suspend fun upsert(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)

    @Query("SELECT * FROM collections")
    suspend fun getAll(): List<CollectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collections: List<CollectionEntity>)

    @Query("DELETE FROM collections")
    suspend fun deleteAll()
}
