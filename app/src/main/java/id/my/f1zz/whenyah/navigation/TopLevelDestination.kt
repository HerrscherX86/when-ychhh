package id.my.f1zz.whenyah.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
) {
    HOME("home", Icons.Outlined.Home),
    COLLECTIONS("collections", Icons.Outlined.Collections),
    SETTINGS("settings", Icons.Outlined.Settings),
}
