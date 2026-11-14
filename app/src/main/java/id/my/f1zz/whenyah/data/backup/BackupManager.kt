package id.my.f1zz.whenyah.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import id.my.f1zz.whenyah.data.local.WhenYahDatabase
import id.my.f1zz.whenyah.data.local.WHEN_YAH_DATABASE_VERSION
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.data.local.entity.PurchaseRecordEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.notification.ReminderCoordinator
import id.my.f1zz.whenyah.data.settings.SettingsRepository
import id.my.f1zz.whenyah.data.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class BackupExportResult(
    val itemCount: Int,
    val imageCount: Int,
)

data class BackupRestoreResult(
    val itemCount: Int,
    val imageCount: Int,
    val settingsApplied: Boolean,
)

class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Singleton
class BackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: WhenYahDatabase,
    private val settingsRepository: SettingsRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val widgetUpdater: WidgetUpdater,
) {
    private val json = Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
        prettyPrint = true
    }

    suspend fun exportTo(uri: Uri): Result<BackupExportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val workFile = File.createTempFile("whenyah-export-", ".zip", context.cacheDir)
        try {
            val collections = database.collectionDao().getAll()
            val items = database.wishlistItemDao().getAll()
            val events = database.savingsEventDao().getAll()
            val plans = database.savingsPlanDao().getAllPlans()
            val purchaseRecords = database.purchaseRecordDao().getAll()
            val settings = settingsRepository.currentSettings()

            val images = items
                .mapNotNull { it.imageRelativePath }
                .distinct()
                .map { relativePath ->
                    val file = resolveStoredImage(relativePath)
                        ?: throw BackupException("Image file is missing: $relativePath")
                    ImageSource(relativePath = relativePath, file = file)
                }

            val data = BackupData(
                collections = collections.map { it.toBackup() },
                items = items.map { it.toBackup() },
                savingsEvents = events.map { it.toBackup() },
                savingsPlans = plans.map { it.toBackup() },
                purchaseRecords = purchaseRecords.map { it.toBackup() },
                settings = settings.toBackup(),
            )
            val dataBytes = json.encodeToString(data).toByteArray(Charsets.UTF_8)
            if (dataBytes.size > MAX_DATA_BYTES) {
                throw BackupException("Backup data is too large")
            }

            ZipOutputStream(
                BufferedOutputStream(FileOutputStream(workFile))
            ).use { zip ->
                writeBytes(zip, BACKUP_DATA_ENTRY, dataBytes)

                val imageEntries = images.map { image ->
                    val zipPath = BACKUP_IMAGE_PREFIX + image.file.name
                    val digest = writeFile(zip, zipPath, image.file)
                    if (digest.size > MAX_IMAGE_BYTES) {
                        throw BackupException("Image is too large: ${image.relativePath}")
                    }
                    BackupImageEntry(
                        relativePath = image.relativePath,
                        zipPath = zipPath,
                        sha256 = digest.sha256,
                        sizeBytes = digest.size,
                    )
                }

                val manifest = BackupManifest(
                    format = BACKUP_FORMAT,
                    formatVersion = BACKUP_FORMAT_VERSION,
                    databaseSchemaVersion = WHEN_YAH_DATABASE_VERSION,
                    appVersionName = appVersionName(),
                    exportedAtEpochMillis = System.currentTimeMillis(),
                    dataFile = BackupFileEntry(
                        path = BACKUP_DATA_ENTRY,
                        sha256 = sha256(dataBytes),
                        sizeBytes = dataBytes.size.toLong(),
                    ),
                    images = imageEntries,
                )
                writeBytes(
                    zip,
                    BACKUP_MANIFEST_ENTRY,
                    json.encodeToString(manifest).toByteArray(Charsets.UTF_8),
                )
            }

            val output = context.contentResolver.openOutputStream(uri, "w")
                ?: throw BackupException("Could not open the selected file for writing")
            output.use { target ->
                workFile.inputStream().use { source -> source.copyTo(target) }
            }

                BackupExportResult(
                    itemCount = items.size,
                    imageCount = images.size,
                )
            } finally {
                workFile.delete()
            }
        }
    }

    suspend fun restoreFrom(uri: Uri): Result<BackupRestoreResult> = withContext(Dispatchers.IO) {
        runCatching {
            val workFile = File.createTempFile("whenyah-restore-", ".zip", context.cacheDir)
        try {
            copyUriToFile(uri, workFile)

            ZipFile(workFile).use { zip ->
                val manifest = readManifest(zip)
                val data = readData(zip, manifest)
                val validated = BackupValidator.validate(data)

                val expectedImages = validated.items
                    .mapNotNull { it.imageRelativePath }
                    .toSet()
                validateImageEntries(zip, manifest, expectedImages)

                val imagePaths = restoreImages(zip, manifest)
                    .toMutableMap()
                val newItems = validated.items.map { item ->
                    item.copy(
                        imageRelativePath = item.imageRelativePath?.let { oldPath ->
                            imagePaths[oldPath]
                                ?: throw BackupException("Image mapping is missing")
                        },
                    )
                }

                val oldImagePaths = database.wishlistItemDao()
                    .getAll()
                    .mapNotNull { it.imageRelativePath }
                    .toSet()

                reminderCoordinator.cancelAllSchedules()

                try {
                    replaceDatabase(
                        collections = validated.collections,
                        items = newItems,
                        events = validated.events,
                        plans = validated.plans,
                        purchaseRecords = validated.purchaseRecords,
                    )
                } catch (failure: Throwable) {
                    imagePaths.values
                        .map { File(context.filesDir, it) }
                        .forEach { it.delete() }
                    runCatching { reminderCoordinator.syncAllSchedules() }
                    throw failure
                }

                val newPaths = imagePaths.values.toSet()
                oldImagePaths
                    .filter { it !in newPaths }
                    .forEach { File(context.filesDir, it).delete() }

                val settingsApplied = runCatching {
                    settingsRepository.replaceSettings(validated.settings)
                }.isSuccess
                runCatching { reminderCoordinator.syncAllSchedules() }
                widgetUpdater.refresh()

                    BackupRestoreResult(
                        itemCount = newItems.size,
                        imageCount = imagePaths.size,
                        settingsApplied = settingsApplied,
                    )
                }
            } finally {
                workFile.delete()
            }
        }
    }

    private fun readManifest(zip: ZipFile): BackupManifest {
        val entry = zip.getEntry(BACKUP_MANIFEST_ENTRY)
            ?: throw BackupException("Backup manifest is missing")
        val bytes = readEntry(zip, entry, MAX_MANIFEST_BYTES)
        val manifest = runCatching {
            json.decodeFromString<BackupManifest>(bytes.toString(Charsets.UTF_8))
        }.getOrElse { throw BackupException("Backup manifest is invalid") }

        if (manifest.format != BACKUP_FORMAT) {
            throw BackupException("Not a When Yah backup")
        }
        if (manifest.formatVersion != BACKUP_FORMAT_VERSION) {
            throw BackupException("Unsupported backup format version")
        }
        if (manifest.databaseSchemaVersion > WHEN_YAH_DATABASE_VERSION) {
            throw BackupException("Backup database version is not supported")
        }
        return manifest
    }

    private fun readData(zip: ZipFile, manifest: BackupManifest): BackupData {
        if (manifest.dataFile.path != BACKUP_DATA_ENTRY) {
            throw BackupException("Backup data path is invalid")
        }
        val entry = zip.getEntry(manifest.dataFile.path)
            ?: throw BackupException("Backup data is missing")
        val bytes = readEntry(zip, entry, MAX_DATA_BYTES)
        if (bytes.size.toLong() != manifest.dataFile.sizeBytes ||
            !sha256(bytes).equals(manifest.dataFile.sha256, ignoreCase = true)
        ) {
            throw BackupException("Backup data checksum failed")
        }

        return runCatching {
            json.decodeFromString<BackupData>(bytes.toString(Charsets.UTF_8))
        }.getOrElse { throw BackupException("Backup data is invalid") }
    }

    private fun validateImageEntries(
        zip: ZipFile,
        manifest: BackupManifest,
        expectedPaths: Set<String>,
    ) {
        val relativePaths = manifest.images.map { it.relativePath }
        if (relativePaths.size != relativePaths.toSet().size) {
            throw BackupException("Backup contains duplicate image paths")
        }
        if (relativePaths.toSet() != expectedPaths) {
            throw BackupException("Backup image references do not match its items")
        }

        var totalBytes = 0L
        manifest.images.forEach { image ->
            if (!BackupValidator.isValidImagePath(image.relativePath) ||
                !BackupValidator.isValidZipImagePath(image.zipPath)
            ) {
                throw BackupException("Backup contains an unsafe image path")
            }
            if (image.sizeBytes <= 0 || image.sizeBytes > MAX_IMAGE_BYTES) {
                throw BackupException("Backup contains an invalid image size")
            }
            totalBytes = Math.addExact(totalBytes, image.sizeBytes)
            if (totalBytes > MAX_TOTAL_IMAGE_BYTES) {
                throw BackupException("Backup images are too large")
            }
        }

        val zipPaths = manifest.images.map { it.zipPath }
        if (zipPaths.size != zipPaths.toSet().size) {
            throw BackupException("Backup contains duplicate image entries")
        }
        manifest.images.forEach { image ->
            if (zip.getEntry(image.zipPath) == null) {
                throw BackupException("Backup image is missing")
            }
        }
    }

    private fun restoreImages(
        zip: ZipFile,
        manifest: BackupManifest,
    ): Map<String, String> {
        val imageDirectory = File(context.filesDir, "item_images").apply { mkdirs() }
        val restored = mutableListOf<File>()
        val mapping = mutableMapOf<String, String>()

        try {
            manifest.images.forEach { image ->
                val extension = extensionFor(image.relativePath)
                val newName = "${UUID.randomUUID()}.$extension"
                val destination = File(imageDirectory, newName)
                restored += destination
                val digest = copyEntryToFile(
                    zip = zip,
                    zipPath = image.zipPath,
                    destination = destination,
                    maxBytes = MAX_IMAGE_BYTES,
                )

                if (digest.size != image.sizeBytes ||
                    !digest.sha256.equals(image.sha256, ignoreCase = true)
                ) {
                    throw BackupException("Backup image checksum failed")
                }
                mapping[image.relativePath] = "$IMAGE_STORAGE_PREFIX$newName"
            }
        } catch (failure: Throwable) {
            restored.forEach { it.delete() }
            throw failure
        }

        return mapping
    }

    private suspend fun replaceDatabase(
        collections: List<CollectionEntity>,
        items: List<WishlistItemEntity>,
        events: List<SavingsEventEntity>,
        plans: List<SavingsPlanEntity>,
        purchaseRecords: List<PurchaseRecordEntity>,
    ) {
        database.withTransaction {
            database.savingsEventDao().deleteAll()
            database.purchaseRecordDao().deleteAll()
            database.savingsPlanDao().deleteAll()
            database.wishlistItemDao().deleteAll()
            database.collectionDao().deleteAll()

            database.collectionDao().insertAll(collections)
            database.wishlistItemDao().insertAll(items)
            database.purchaseRecordDao().insertAll(purchaseRecords)
            database.savingsPlanDao().insertAll(plans)
            database.savingsEventDao().insertAll(events)
        }
    }

    private fun copyUriToFile(uri: Uri, destination: File) {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw BackupException("Could not open the selected backup file")
        input.use { source ->
            destination.outputStream().use { target ->
                copyWithLimit(source, target, MAX_BACKUP_BYTES)
            }
        }
    }

    private fun readEntry(zip: ZipFile, entry: ZipEntry, maxBytes: Long): ByteArray {
        if (entry.size > maxBytes) throw BackupException("Backup entry is too large")
        val output = ByteArrayOutputStream()
        zip.getInputStream(entry).use { input ->
            copyWithLimit(input, output, maxBytes)
        }
        return output.toByteArray()
    }

    private fun copyEntryToFile(
        zip: ZipFile,
        zipPath: String,
        destination: File,
        maxBytes: Long,
    ): EntryDigest {
        val entry = zip.getEntry(zipPath)
            ?: throw BackupException("Backup image is missing")
        if (entry.size > maxBytes) throw BackupException("Backup image is too large")

        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        zip.getInputStream(entry).use { input ->
            BufferedOutputStream(FileOutputStream(destination)).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    size += read
                    if (size > maxBytes) throw BackupException("Backup image is too large")
                    digest.update(buffer, 0, read)
                    output.write(buffer, 0, read)
                }
            }
        }
        return EntryDigest(size = size, sha256 = digest.digest().toHex())
    }

    private fun writeBytes(zip: ZipOutputStream, path: String, bytes: ByteArray) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(bytes)
        zip.closeEntry()
    }

    private fun writeFile(zip: ZipOutputStream, path: String, file: File): EntryDigest {
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        zip.putNextEntry(ZipEntry(path))
        BufferedInputStream(file.inputStream()).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                size += read
                digest.update(buffer, 0, read)
                zip.write(buffer, 0, read)
            }
        }
        zip.closeEntry()
        return EntryDigest(size = size, sha256 = digest.digest().toHex())
    }

    private fun copyWithLimit(
        input: InputStream,
        output: java.io.OutputStream,
        maxBytes: Long,
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) throw BackupException("Backup is too large")
            output.write(buffer, 0, read)
        }
    }

    private fun resolveStoredImage(relativePath: String): File? {
        if (!BackupValidator.isValidImagePath(relativePath)) return null
        val imageDirectory = File(context.filesDir, "item_images").canonicalFile
        val file = File(context.filesDir, relativePath).canonicalFile
        if (!file.path.startsWith(imageDirectory.path + File.separator)) return null
        return file.takeIf { it.isFile }
    }

    private fun extensionFor(relativePath: String): String {
        val extension = relativePath.substringAfterLast('.', "")
        return extension.takeIf { it.matches(Regex("[A-Za-z0-9]{1,8}")) } ?: "jpg"
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    @Suppress("DEPRECATION")
    private fun appVersionName(): String = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.versionName ?: "unknown"
    }.getOrDefault("unknown")

    private data class ImageSource(
        val relativePath: String,
        val file: File,
    )

    private data class EntryDigest(
        val size: Long,
        val sha256: String,
    )

    private companion object {
        const val MAX_MANIFEST_BYTES = 1L * 1024 * 1024
        const val MAX_DATA_BYTES = 50L * 1024 * 1024
        const val MAX_IMAGE_BYTES = 25L * 1024 * 1024
        const val MAX_TOTAL_IMAGE_BYTES = 300L * 1024 * 1024
        const val MAX_BACKUP_BYTES = 350L * 1024 * 1024
    }
}

private fun CollectionEntity.toBackup() = BackupCollection(
    id = id.toString(),
    name = name,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)

private fun WishlistItemEntity.toBackup() = BackupItem(
    id = id.toString(),
    name = name,
    targetPriceIdr = targetPriceIdr,
    imageRelativePath = imageRelativePath,
    purchaseUrl = purchaseUrl,
    notes = notes,
    category = category,
    collectionId = collectionId?.toString(),
    status = status.name,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
    purchasedAtEpochMillis = purchasedAt?.toEpochMilli(),
)

private fun SavingsEventEntity.toBackup() = BackupSavingsEvent(
    id = id.toString(),
    itemId = itemId.toString(),
    type = type.name,
    amountIdr = amountIdr,
    note = note,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    purchaseRecordId = purchaseRecordId?.toString(),
)

private fun SavingsPlanEntity.toBackup() = BackupSavingsPlan(
    id = id.toString(),
    itemId = itemId.toString(),
    contributionIdr = contributionIdr,
    frequency = frequency.name,
    reminderEnabled = reminderEnabled,
    reminderLocalTime = reminderLocalTime?.toString(),
    active = active,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
    reminderAnchorDate = reminderAnchorDate?.toString(),
)

private fun PurchaseRecordEntity.toBackup() = BackupPurchaseRecord(
    id = id.toString(),
    itemId = itemId.toString(),
    completedAtEpochMillis = completedAt.toEpochMilli(),
    savingsSpentIdr = savingsSpentIdr,
)

private fun id.my.f1zz.whenyah.domain.settings.AppSettings.toBackup() = BackupSettings(
    language = language.name,
    themeMode = themeMode.name,
    accentColor = accentColor.name,
    useDynamicColor = useDynamicColor,
    notificationsEnabled = notificationsEnabled,
    goalNotificationsEnabled = goalNotificationsEnabled,
    summaryCadence = summaryCadence.name,
    summaryTime = summaryTime.toString(),
    summaryAnchorDate = summaryAnchorDate?.toString(),
)
