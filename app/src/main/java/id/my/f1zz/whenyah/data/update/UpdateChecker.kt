package id.my.f1zz.whenyah.data.update

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun installedVersionCode(): Long = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
    }.getOrDefault(0L)

    suspend fun fetchManifest(): UpdateManifest? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(MANIFEST_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("User-Agent", "WhenYah-Android")
                setRequestProperty("Accept", "application/json")
            }

            try {
                if (connection.responseCode !in 200..299) return@runCatching null

                val body = connection.inputStream
                    .bufferedReader(StandardCharsets.UTF_8)
                    .use { reader ->
                        val builder = StringBuilder()
                        val buffer = CharArray(4096)
                        while (true) {
                            val read = reader.read(buffer)
                            if (read < 0) break
                            builder.append(buffer, 0, read)
                            if (builder.length > MAX_MANIFEST_CHARS) {
                                return@runCatching null
                            }
                        }
                        builder.toString()
                    }

                json.decodeFromString<UpdateManifest>(body)
                    .takeIf { manifest ->
                        manifest.versionCode > 0 &&
                            manifest.versionName.isNotBlank() &&
                            isAllowedReleaseUrl(manifest.releaseUrl)
                    }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun isAllowedReleaseUrl(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme == "https" &&
            uri.host == "github.com" &&
            uri.path.orEmpty().startsWith("/HerrscherX86/when-ychhh/")
    }.getOrDefault(false)

    private companion object {
        const val MANIFEST_URL =
            "https://github.com/HerrscherX86/when-ychhh/releases/latest/download/update.json"
        const val TIMEOUT_MILLIS = 5_000
        const val MAX_MANIFEST_CHARS = 64 * 1024
    }
}
