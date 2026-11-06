package id.my.f1zz.whenyah.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun findById(id: UUID): CollectionEntity?

    @Upsert
    suspend fun upsert(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)
}
