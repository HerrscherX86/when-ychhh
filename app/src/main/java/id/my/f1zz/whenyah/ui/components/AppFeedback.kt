package id.my.f1zz.whenyah.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * App-wide transient feedback. The snackbar host lives in [WhenYahApp], so
 * messages survive navigation between screens and dialogs.
 */
class AppFeedback(
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val haptics: HapticFeedback,
) {
    fun success(message: String) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        show(message)
    }

    fun error(message: String) {
        haptics.performHapticFeedback(HapticFeedbackType.Reject)
        show(message)
    }

    fun info(message: String) = show(message)

    private fun show(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
}

val LocalAppFeedback = staticCompositionLocalOf<AppFeedback> {
    error("AppFeedback is not provided")
}
