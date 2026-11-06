package id.my.f1zz.whenyah.data.backup

import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.data.local.entity.PurchaseRecordEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.AppSettings
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

data class ValidatedBackup(
    val collections: List<CollectionEntity>,
    val items: List<WishlistItemEntity>,
    val events: List<SavingsEventEntity>,
    val plans: List<SavingsPlanEntity>,
    val purchaseRecords: List<PurchaseRecordEntity>,
    val settings: AppSettings,
)

class BackupValidationException(message: String) : Exception(message)

object BackupValidator {
    fun validate(data: BackupData): ValidatedBackup {
        val collections = data.collections.map { collection ->
            CollectionEntity(
                id = parseUuid(collection.id, "collection id"),
                name = collection.name.requireNotBlank("collection name"),
                createdAt = instant(collection.createdAtEpochMillis, "collection createdAt"),
                updatedAt = instant(collection.updatedAtEpochMillis, "collection updatedAt"),
            )
        }
        requireUnique(collections.map { it.id }, "collection id")
        val collectionIds = collections.mapTo(mutableSetOf()) { it.id }

        val purchaseRecords = data.purchaseRecords.map { record ->
            val spent = record.savingsSpentIdr
            if (spent < 0) fail("purchase record savingsSpentIdr must be nonnegative")
            PurchaseRecordEntity(
                id = parseUuid(record.id, "purchase record id"),
                itemId = parseUuid(record.itemId, "purchase record itemId"),
                completedAt = instant(record.completedAtEpochMillis, "purchase completedAt"),
                savingsSpentIdr = spent,
            )
        }
        requireUnique(purchaseRecords.map { it.id }, "purchase record id")

        val items = data.items.map { item ->
            val target = item.targetPriceIdr
            if (target < 0) fail("item targetPriceIdr must be nonnegative")
            val collectionId = item.collectionId?.let {
                parseUuid(it, "item collectionId").also { id ->
                    if (id !in collectionIds) fail("item references missing collection")
                }
            }
            val imagePath = item.imageRelativePath
            if (imagePath != null && !isValidImagePath(imagePath)) {
                fail("invalid image path")
            }

            WishlistItemEntity(
                id = parseUuid(item.id, "item id"),
                name = item.name.requireNotBlank("item name"),
                targetPriceIdr = target,
                imageRelativePath = imagePath,
                purchaseUrl = item.purchaseUrl,
                notes = item.notes,
                category = item.category,
                collectionId = collectionId,
                status = parseEnum<ItemStatus>(item.status, "item status"),
                createdAt = instant(item.createdAtEpochMillis, "item createdAt"),
                updatedAt = instant(item.updatedAtEpochMillis, "item updatedAt"),
                purchasedAt = item.purchasedAtEpochMillis?.let {
                    instant(it, "item purchasedAt")
                },
            )
        }
        requireUnique(items.map { it.id }, "item id")
        val itemIds = items.mapTo(mutableSetOf()) { it.id }
        val purchaseRecordIds = purchaseRecords.mapTo(mutableSetOf()) { it.id }

        purchaseRecords.forEach { record ->
            if (record.itemId !in itemIds) fail("purchase record references missing item")
        }

        val events = data.savingsEvents.map { event ->
            val amount = event.amountIdr
            if (amount <= 0) fail("savings event amountIdr must be positive")
            val itemId = parseUuid(event.itemId, "savings event itemId")
            if (itemId !in itemIds) fail("savings event references missing item")
            val purchaseRecordId = event.purchaseRecordId?.let {
                parseUuid(it, "savings event purchaseRecordId").also { id ->
                    if (id !in purchaseRecordIds) {
                        fail("savings event references missing purchase record")
                    }
                }
            }

            SavingsEventEntity(
                id = parseUuid(event.id, "savings event id"),
                itemId = itemId,
                type = parseEnum<SavingsEventType>(event.type, "savings event type"),
                amountIdr = amount,
                note = event.note,
                createdAt = instant(event.createdAtEpochMillis, "savings event createdAt"),
                purchaseRecordId = purchaseRecordId,
            )
        }
        requireUnique(events.map { it.id }, "savings event id")

        val plans = data.savingsPlans.map { plan ->
            val contribution = plan.contributionIdr
            if (contribution <= 0) fail("plan contributionIdr must be positive")
            val itemId = parseUuid(plan.itemId, "plan itemId")
            if (itemId !in itemIds) fail("plan references missing item")
            val reminderTime = plan.reminderLocalTime?.let { raw ->
                runCatching { LocalTime.parse(raw) }
                    .getOrElse { fail("invalid plan reminderLocalTime") }
            }
            if (plan.reminderEnabled && reminderTime == null) {
                fail("enabled plan reminder requires a time")
            }

            SavingsPlanEntity(
                id = parseUuid(plan.id, "plan id"),
                itemId = itemId,
                contributionIdr = contribution,
                frequency = parseEnum<PlanFrequency>(plan.frequency, "plan frequency"),
                reminderEnabled = plan.reminderEnabled,
                reminderLocalTime = reminderTime,
                active = plan.active,
                createdAt = instant(plan.createdAtEpochMillis, "plan createdAt"),
                updatedAt = instant(plan.updatedAtEpochMillis, "plan updatedAt"),
            )
        }
        requireUnique(plans.map { it.id }, "plan id")
        requireUnique(plans.map { it.itemId }, "plan itemId")

        val settings = AppSettings(
            themeMode = parseEnum<ThemeMode>(data.settings.themeMode, "theme mode"),
            accentColor = parseEnum<AccentColor>(data.settings.accentColor, "accent color"),
            useDynamicColor = data.settings.useDynamicColor,
            notificationsEnabled = data.settings.notificationsEnabled,
            goalNotificationsEnabled = data.settings.goalNotificationsEnabled,
            summaryCadence = parseEnum<SummaryCadence>(
                data.settings.summaryCadence,
                "summary cadence",
            ),
            summaryTime = runCatching { LocalTime.parse(data.settings.summaryTime) }
                .getOrElse { fail("invalid summary time") },
        )

        val referencedImages = items.mapNotNullTo(mutableSetOf()) { it.imageRelativePath }
        if (referencedImages.any { !isValidImagePath(it) }) {
            fail("invalid image path")
        }

        return ValidatedBackup(
            collections = collections,
            items = items,
            events = events,
            plans = plans,
            purchaseRecords = purchaseRecords,
            settings = settings,
        )
    }

    fun isValidImagePath(path: String): Boolean {
        if (!path.startsWith(IMAGE_STORAGE_PREFIX)) return false
        val name = path.removePrefix(IMAGE_STORAGE_PREFIX)
        return name.isNotBlank() &&
            '/' !in name &&
            '\\' !in name &&
            name != "." &&
            name != ".." &&
            !name.contains("..")
    }

    fun isValidZipImagePath(path: String): Boolean {
        if (!path.startsWith(BACKUP_IMAGE_PREFIX)) return false
        val name = path.removePrefix(BACKUP_IMAGE_PREFIX)
        return name.isNotBlank() &&
            '/' !in name &&
            '\\' !in name &&
            name != "." &&
            name != ".." &&
            !name.contains("..")
    }

    private fun parseUuid(value: String, field: String): UUID =
        runCatching { UUID.fromString(value) }
            .getOrElse { fail("invalid $field") }

    private fun instant(value: Long, field: String): Instant =
        runCatching { Instant.ofEpochMilli(value) }
            .getOrElse { fail("invalid $field") }

    private inline fun <reified T : Enum<T>> parseEnum(value: String, field: String): T =
        runCatching { enumValueOf<T>(value) }
            .getOrElse { fail("invalid $field") }

    private fun String.requireNotBlank(field: String): String {
        if (isBlank()) fail("$field cannot be blank")
        return this
    }

    private fun requireUnique(values: List<UUID>, field: String) {
        if (values.size != values.toSet().size) fail("duplicate $field")
    }

    private fun fail(message: String): Nothing = throw BackupValidationException(message)
}
