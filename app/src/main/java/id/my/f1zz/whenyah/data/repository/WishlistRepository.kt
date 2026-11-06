package id.my.f1zz.whenyah.data.repository

import android.net.Uri
import id.my.f1zz.whenyah.data.image.ItemImageStore
import id.my.f1zz.whenyah.data.local.dao.CollectionDao
import id.my.f1zz.whenyah.data.local.dao.SavingsEventDao
import id.my.f1zz.whenyah.data.local.dao.WishlistItemDao
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.local.model.CollectionWithItemCount
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.savings.SavingsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val itemDao: WishlistItemDao,
    private val collectionDao: CollectionDao,
    private val savingsEventDao: SavingsEventDao,
    private val imageStore: ItemImageStore,
) {
    fun observeActiveItemsWithSavings(): Flow<List<WishlistItemWithSavings>> =
        combine(itemDao.observeActive(), savingsEventDao.observeAll()) { items, events ->
            val eventsByItem = events.groupBy { it.itemId }
            items.map { item ->
                WishlistItemWithSavings(
                    item = item,
                    savings = SavingsCalculator.summarize(
                        entries = eventsByItem[item.id].orEmpty(),
                        targetPriceIdr = item.targetPriceIdr,
                    ),
                )
            }
        }

    fun observeItemsInCollectionWithSavings(
        collectionId: UUID,
    ): Flow<List<WishlistItemWithSavings>> = combine(
        itemDao.observeActiveByCollection(collectionId),
        savingsEventDao.observeAll(),
    ) { items, events ->
        val itemIds = items.mapTo(mutableSetOf()) { it.id }
        val eventsByItem = events
            .filter { it.itemId in itemIds }
            .groupBy { it.itemId }
        items.map { item ->
            WishlistItemWithSavings(
                item = item,
                savings = SavingsCalculator.summarize(
                    entries = eventsByItem[item.id].orEmpty(),
                    targetPriceIdr = item.targetPriceIdr,
                ),
            )
        }
    }

    fun observeItem(itemId: UUID): Flow<WishlistItemEntity?> = itemDao.observeById(itemId)

    fun observeCollections(): Flow<List<CollectionWithItemCount>> =
        collectionDao.observeAllWithItemCount()

    fun observeCollection(collectionId: UUID): Flow<CollectionEntity?> =
        collectionDao.observeById(collectionId)

    suspend fun getItem(itemId: UUID): WishlistItemEntity? = itemDao.findById(itemId)

    suspend fun saveItem(
        itemId: UUID? = null,
        name: String,
        targetPriceIdr: Long,
        imageRelativePath: String?,
        purchaseUrl: String?,
        notes: String?,
        category: String?,
        collectionId: UUID?,
    ): UUID {
        val now = Instant.now()
        val existing = itemId?.let { itemDao.findById(it) }
        if (itemId != null && existing == null) {
            error("Item not found")
        }

        val id = existing?.id ?: UUID.randomUUID()
        val normalizedUrl = purchaseUrl?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedNotes = notes?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedCategory = category?.trim()?.takeIf { it.isNotEmpty() }

        val entity = existing?.copy(
            name = name.trim(),
            targetPriceIdr = targetPriceIdr,
            imageRelativePath = imageRelativePath,
            purchaseUrl = normalizedUrl,
            notes = normalizedNotes,
            category = normalizedCategory,
            collectionId = collectionId,
            updatedAt = now,
        ) ?: WishlistItemEntity(
            id = id,
            name = name.trim(),
            targetPriceIdr = targetPriceIdr,
            imageRelativePath = imageRelativePath,
            purchaseUrl = normalizedUrl,
            notes = normalizedNotes,
            category = normalizedCategory,
            collectionId = collectionId,
            status = ItemStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
            purchasedAt = null,
        )

        itemDao.upsert(entity)

        val previousImage = existing?.imageRelativePath
        if (previousImage != null && previousImage != imageRelativePath) {
            imageStore.delete(previousImage)
        }

        return id
    }

    suspend fun deleteItem(itemId: UUID) {
        val item = itemDao.findById(itemId) ?: return
        itemDao.deleteById(itemId)
        imageStore.delete(item.imageRelativePath)
    }

    suspend fun createCollection(name: String): UUID {
        val now = Instant.now()
        val id = UUID.randomUUID()
        collectionDao.upsert(
            CollectionEntity(
                id = id,
                name = name.trim(),
                createdAt = now,
                updatedAt = now,
            )
        )
        return id
    }

    suspend fun renameCollection(collectionId: UUID, name: String) {
        val existing = collectionDao.findById(collectionId) ?: return
        collectionDao.upsert(
            existing.copy(
                name = name.trim(),
                updatedAt = Instant.now(),
            )
        )
    }

    suspend fun deleteCollection(collectionId: UUID) {
        val collection = collectionDao.findById(collectionId) ?: return
        collectionDao.delete(collection)
    }

    suspend fun copyImage(uri: Uri): String = imageStore.copyFromUri(uri)

    suspend fun deleteImage(relativePath: String?) = imageStore.delete(relativePath)
}
