package id.my.f1zz.whenyah.data.update

import id.my.f1zz.whenyah.domain.update.UpdatePolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class ManualUpdateState {
    Idle,
    Checking,
    UpToDate,
    Failed,
}

@Singleton
class UpdateManager @Inject constructor(
    private val checker: UpdateChecker,
    private val preferences: UpdatePreferences,
) {
    private val checkMutex = Mutex()

    private val _pendingUpdate = MutableStateFlow<UpdateManifest?>(null)
    val pendingUpdate: StateFlow<UpdateManifest?> = _pendingUpdate.asStateFlow()

    private val _manualState = MutableStateFlow(ManualUpdateState.Idle)
    val manualState: StateFlow<ManualUpdateState> = _manualState.asStateFlow()

    suspend fun checkAutomatically() {
        if (!preferences.updateChecksEnabled.first()) return

        checkMutex.withLock {
            val now = System.currentTimeMillis()
            if (now - preferences.lastCheckedAt() < CHECK_INTERVAL_MILLIS) return

            preferences.setLastCheckedAt(now)
            val manifest = checker.fetchManifest() ?: return
            evaluate(manifest = manifest, manual = false, now = now)
        }
    }

    suspend fun checkManually() {
        _manualState.value = ManualUpdateState.Checking

        checkMutex.withLock {
            val now = System.currentTimeMillis()
            preferences.setLastCheckedAt(now)
            val manifest = checker.fetchManifest()

            if (manifest == null) {
                _manualState.value = ManualUpdateState.Failed
                return
            }

            val prompted = evaluate(manifest = manifest, manual = true, now = now)
            _manualState.value = if (prompted) {
                ManualUpdateState.Idle
            } else {
                ManualUpdateState.UpToDate
            }
        }
    }

    fun dismissPendingUpdate() {
        _pendingUpdate.value = null
    }

    suspend fun skipPendingUpdate() {
        val pending = _pendingUpdate.value ?: return
        preferences.recordSkip(pending.versionCode)
        _pendingUpdate.value = null
    }

    private suspend fun evaluate(
        manifest: UpdateManifest,
        manual: Boolean,
        now: Long,
    ): Boolean {
        val shouldPrompt = UpdatePolicy.shouldPrompt(
            remoteVersionCode = manifest.versionCode,
            installedVersionCode = checker.installedVersionCode(),
            lastPromptedVersionCode = preferences.lastPromptedVersionCode(),
            lastPromptedAtEpochMillis = preferences.lastPromptedAt(),
            nowEpochMillis = now,
            snoozeMillis = SNOOZE_MILLIS,
            manual = manual,
            skippedVersionCode = preferences.skippedVersionCode(),
        )

        if (shouldPrompt) {
            preferences.recordPrompt(manifest.versionCode, now)
            _pendingUpdate.value = manifest
        }
        return shouldPrompt
    }

    private companion object {
        val CHECK_INTERVAL_MILLIS = TimeUnit.HOURS.toMillis(24)
        val SNOOZE_MILLIS = TimeUnit.DAYS.toMillis(3)
    }
}
