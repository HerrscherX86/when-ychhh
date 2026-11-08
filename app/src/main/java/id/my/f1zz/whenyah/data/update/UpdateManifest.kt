package id.my.f1zz.whenyah.data.update

import kotlinx.serialization.Serializable

@Serializable
data class UpdateManifest(
    val versionCode: Long,
    val versionName: String,
    val releaseUrl: String,
    val notes: String? = null,
)
