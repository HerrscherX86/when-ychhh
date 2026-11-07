package id.my.f1zz.whenyah.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Screens can register an optional handler for the shared top-bar back action.
 * Passing null restores the default navigation behavior.
 */
val LocalSetTopBarBackHandler = staticCompositionLocalOf<((() -> Unit)?) -> Unit> { {} }
