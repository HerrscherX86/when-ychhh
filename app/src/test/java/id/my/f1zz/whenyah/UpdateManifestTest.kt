package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.data.update.UpdateManifest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateManifestTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesManifestAndIgnoresUnknownFields() {
        val manifest = json.decodeFromString<UpdateManifest>(
            """
            {
              "versionCode": 2,
              "versionName": "1.1",
              "releaseUrl": "https://github.com/HerrscherX86/when-ychhh/releases/latest",
              "futureField": "ignored"
            }
            """.trimIndent()
        )

        assertEquals(2L, manifest.versionCode)
        assertEquals("1.1", manifest.versionName)
        assertNull(manifest.notes)
    }
}
