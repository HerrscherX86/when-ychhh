package id.my.f1zz.whenyah.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Outlined.Home),
    COLLECTIONS("collections", "Collections", Icons.Outlined.Collections),
    SETTINGS("settings", "Settings", Icons.Outlined.Settings),
}
