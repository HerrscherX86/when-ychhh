package id.my.f1zz.whenyah.data.image

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemImageStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun resolve(relativePath: String?): File? {
        val path = relativePath ?: return null
        return File(context.filesDir, path).takeIf { it.isFile }
    }

    suspend fun copyFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val extension = extensionFor(context.contentResolver.getType(uri))
        val fileName = "${UUID.randomUUID()}.$extension"
        val destination = File(imagesDirectory(), fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open the selected image")

        "$IMAGE_DIRECTORY/$fileName"
    }

    suspend fun delete(relativePath: String?) = withContext(Dispatchers.IO) {
        relativePath?.let { File(context.filesDir, it).delete() }
        Unit
    }

    private fun imagesDirectory(): File =
        File(context.filesDir, IMAGE_DIRECTORY).apply { mkdirs() }

    private fun extensionFor(mimeType: String?): String = when (mimeType) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }

    private companion object {
        const val IMAGE_DIRECTORY = "item_images"
    }
}
