package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.domain.update.UpdatePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class UpdatePolicyTest {
    private val day = TimeUnit.DAYS.toMillis(1)
    private val snooze = TimeUnit.DAYS.toMillis(3)
    private val now = 1_800_000_000_000L
    private val installed = 2L
    private val remote = 3L

    @Test
    fun newerVersionPrompts() {
        assertTrue(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote,
                installedVersionCode = installed,
                lastPromptedVersionCode = 0L,
                lastPromptedAtEpochMillis = 0L,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
            )
        )
    }

    @Test
    fun sameOrOlderVersionDoesNotPrompt() {
        assertFalse(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = installed,
                installedVersionCode = installed,
                lastPromptedVersionCode = 0L,
                lastPromptedAtEpochMillis = 0L,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
            )
        )
        assertFalse(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = installed - 1,
                installedVersionCode = installed,
                lastPromptedVersionCode = 0L,
                lastPromptedAtEpochMillis = 0L,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
            )
        )
    }

    @Test
    fun sameVersionIsSnoozedWithinThreeDays() {
        assertFalse(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote,
                installedVersionCode = installed,
                lastPromptedVersionCode = remote,
                lastPromptedAtEpochMillis = now - (2 * day),
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
            )
        )
    }

    @Test
    fun sameVersionPromptsAgainAfterSnooze() {
        assertTrue(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote,
                installedVersionCode = installed,
                lastPromptedVersionCode = remote,
                lastPromptedAtEpochMillis = now - (3 * day),
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
            )
        )
    }

    @Test
    fun newVersionPromptsEvenWhenAnotherVersionWasSnoozed() {
        assertTrue(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote + 1,
                installedVersionCode = installed,
                lastPromptedVersionCode = remote,
                lastPromptedAtEpochMillis = now - day,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
            )
        )
    }

    @Test
    fun manualCheckBypassesSnooze() {
        assertTrue(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote,
                installedVersionCode = installed,
                lastPromptedVersionCode = remote,
                lastPromptedAtEpochMillis = now - day,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = true,
            )
        )
    }

    @Test
    fun skippedVersionDoesNotPromptAutomatically() {
        assertFalse(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote,
                installedVersionCode = installed,
                lastPromptedVersionCode = 0L,
                lastPromptedAtEpochMillis = 0L,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
                skippedVersionCode = remote,
            )
        )
    }

    @Test
    fun manualCheckBypassesSkippedVersion() {
        assertTrue(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote,
                installedVersionCode = installed,
                lastPromptedVersionCode = 0L,
                lastPromptedAtEpochMillis = 0L,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = true,
                skippedVersionCode = remote,
            )
        )
    }

    @Test
    fun newVersionPromptsEvenIfOlderVersionWasSkipped() {
        assertTrue(
            UpdatePolicy.shouldPrompt(
                remoteVersionCode = remote + 1,
                installedVersionCode = installed,
                lastPromptedVersionCode = 0L,
                lastPromptedAtEpochMillis = 0L,
                nowEpochMillis = now,
                snoozeMillis = snooze,
                manual = false,
                skippedVersionCode = remote,
            )
        )
    }
}
