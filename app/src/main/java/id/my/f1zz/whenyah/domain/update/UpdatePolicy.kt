package id.my.f1zz.whenyah.domain.update

object UpdatePolicy {
    /**
     * Decides whether the update dialog should be shown.
     *
     * Automatic checks respect the snooze window per remote version. A manual
     * check always shows an available update, even if the dialog was snoozed.
     */
    fun shouldPrompt(
        remoteVersionCode: Long,
        installedVersionCode: Long,
        lastPromptedVersionCode: Long,
        lastPromptedAtEpochMillis: Long,
        nowEpochMillis: Long,
        snoozeMillis: Long,
        manual: Boolean,
    ): Boolean {
        if (remoteVersionCode <= installedVersionCode) return false
        if (manual) return true
        if (lastPromptedVersionCode != remoteVersionCode) return true
        return nowEpochMillis - lastPromptedAtEpochMillis >= snoozeMillis
    }
}
