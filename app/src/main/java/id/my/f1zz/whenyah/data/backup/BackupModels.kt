package id.my.f1zz.whenyah.data.backup

import kotlinx.serialization.Serializable

const val BACKUP_FORMAT = "whenyah-backup"
const val BACKUP_FORMAT_VERSION = 1
const val BACKUP_MANIFEST_ENTRY = "manifest.json"
const val BACKUP_DATA_ENTRY = "data.json"
const val BACKUP_IMAGE_PREFIX = "images/"
const val IMAGE_STORAGE_PREFIX = "item_images/"

@Serializable
data class BackupManifest(
    val format: String,
    val formatVersion: Int,
    val databaseSchemaVersion: Int,
    val appVersionName: String,
    val exportedAtEpochMillis: Long,
    val dataFile: BackupFileEntry,
    val images: List<BackupImageEntry>,
)

@Serializable
data class BackupFileEntry(
    val path: String,
    val sha256: String,
    val sizeBytes: Long,
)

@Serializable
data class BackupImageEntry(
    val relativePath: String,
    val zipPath: String,
    val sha256: String,
    val sizeBytes: Long,
)

@Serializable
data class BackupData(
    val collections: List<BackupCollection>,
    val items: List<BackupItem>,
    val savingsEvents: List<BackupSavingsEvent>,
    val savingsPlans: List<BackupSavingsPlan>,
    val purchaseRecords: List<BackupPurchaseRecord>,
    val settings: BackupSettings,
)

@Serializable
data class BackupCollection(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Serializable
data class BackupItem(
    val id: String,
    val name: String,
    val targetPriceIdr: Long,
    val imageRelativePath: String?,
    val purchaseUrl: String?,
    val notes: String?,
    val category: String?,
    val collectionId: String?,
    val status: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val purchasedAtEpochMillis: Long?,
)

@Serializable
data class BackupSavingsEvent(
    val id: String,
    val itemId: String,
    val type: String,
    val amountIdr: Long,
    val note: String?,
    val createdAtEpochMillis: Long,
    val purchaseRecordId: String?,
)

@Serializable
data class BackupSavingsPlan(
    val id: String,
    val itemId: String,
    val contributionIdr: Long,
    val frequency: String,
    val reminderEnabled: Boolean,
    val reminderLocalTime: String?,
    val active: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val reminderAnchorDate: String? = null,
)

@Serializable
data class BackupPurchaseRecord(
    val id: String,
    val itemId: String,
    val completedAtEpochMillis: Long,
    val savingsSpentIdr: Long,
)

@Serializable
data class BackupSettings(
    val language: String = "ENGLISH",
    val themeMode: String,
    val accentColor: String,
    val useDynamicColor: Boolean,
    val notificationsEnabled: Boolean,
    val goalNotificationsEnabled: Boolean,
    val summaryCadence: String,
    val summaryTime: String,
    val summaryAnchorDate: String? = null,
)
