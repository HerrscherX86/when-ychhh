package id.my.f1zz.whenyah.data.update

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.updateDataStore by preferencesDataStore(name = "update_state")

@Singleton
class UpdatePreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val preferencesFlow = context.updateDataStore.data
        .catch { failure ->
            if (failure is IOException) {
                emit(emptyPreferences())
            } else {
                throw failure
            }
        }

    val updateChecksEnabled: Flow<Boolean> = preferencesFlow
        .map { preferences -> preferences[Keys.CHECKS_ENABLED] ?: true }

    suspend fun setUpdateChecksEnabled(value: Boolean) {
        context.updateDataStore.edit { it[Keys.CHECKS_ENABLED] = value }
    }

    suspend fun lastCheckedAt(): Long =
        preferencesFlow.first()[Keys.LAST_CHECKED_AT] ?: 0L

    suspend fun setLastCheckedAt(value: Long) {
        context.updateDataStore.edit { it[Keys.LAST_CHECKED_AT] = value }
    }

    suspend fun lastPromptedVersionCode(): Long =
        preferencesFlow.first()[Keys.LAST_PROMPTED_VERSION_CODE] ?: 0L

    suspend fun lastPromptedAt(): Long =
        preferencesFlow.first()[Keys.LAST_PROMPTED_AT] ?: 0L

    suspend fun recordPrompt(versionCode: Long, promptedAtEpochMillis: Long) {
        context.updateDataStore.edit { preferences ->
            preferences[Keys.LAST_PROMPTED_VERSION_CODE] = versionCode
            preferences[Keys.LAST_PROMPTED_AT] = promptedAtEpochMillis
        }
    }

    private object Keys {
        val CHECKS_ENABLED = booleanPreferencesKey("checks_enabled")
        val LAST_CHECKED_AT = longPreferencesKey("last_checked_at")
        val LAST_PROMPTED_VERSION_CODE = longPreferencesKey("last_prompted_version_code")
        val LAST_PROMPTED_AT = longPreferencesKey("last_prompted_at")
    }
}
