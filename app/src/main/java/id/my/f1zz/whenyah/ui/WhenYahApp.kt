package id.my.f1zz.whenyah.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.my.f1zz.whenyah.navigation.Routes
import id.my.f1zz.whenyah.navigation.TopLevelDestination
import id.my.f1zz.whenyah.ui.archive.ArchiveScreen
import id.my.f1zz.whenyah.ui.archive.ArchivedItemDetailScreen
import id.my.f1zz.whenyah.ui.collections.CollectionDetailScreen
import id.my.f1zz.whenyah.ui.collections.CollectionsScreen
import id.my.f1zz.whenyah.ui.home.HomeScreen
import id.my.f1zz.whenyah.ui.item.ItemDetailScreen
import id.my.f1zz.whenyah.ui.item.ItemEditorScreen
import id.my.f1zz.whenyah.ui.item.SavingsPlanScreen
import id.my.f1zz.whenyah.ui.item.TransactionHistoryScreen
import id.my.f1zz.whenyah.ui.settings.SettingsScreen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhenYahApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentTopLevelDestination = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }
    val isTopLevel = currentTopLevelDestination != null

    val title = when (currentRoute) {
        Routes.ITEM_DETAIL -> "Item details"
        Routes.ITEM_HISTORY -> "Transaction history"
        Routes.ITEM_PLAN -> "Savings plan"
        Routes.ITEM_ADD -> "Add item"
        Routes.ITEM_EDIT -> "Edit item"
        Routes.COLLECTION_DETAIL -> "Collection"
        Routes.ARCHIVE -> "Purchased archive"
        Routes.ARCHIVE_DETAIL -> "Archived item"
        else -> currentTopLevelDestination?.label ?: "When Yah"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any {
                                it.route == destination.route
                            } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.HOME.route) {
                HomeScreen(
                    onAddItem = { navController.navigate(Routes.itemAdd()) },
                    onItemClick = { itemId ->
                        navController.navigate(Routes.itemDetail(itemId))
                    },
                    onOpenArchive = { navController.navigate(Routes.ARCHIVE) },
                )
            }
            composable(TopLevelDestination.COLLECTIONS.route) {
                CollectionsScreen(
                    onCollectionClick = { collectionId ->
                        navController.navigate(Routes.collectionDetail(collectionId))
                    },
                )
            }
            composable(TopLevelDestination.SETTINGS.route) {
                SettingsScreen()
            }

            composable(
                route = Routes.COLLECTION_DETAIL,
                arguments = listOf(
                    navArgument("collectionId") { type = NavType.StringType },
                ),
            ) {
                CollectionDetailScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Routes.itemDetail(itemId))
                    },
                    onAddItem = { collectionId ->
                        navController.navigate(Routes.itemAdd(collectionId))
                    },
                )
            }

            composable(
                route = Routes.ITEM_DETAIL,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                ),
            ) { entry ->
                val itemId = checkNotNull(entry.arguments?.getString("itemId"))
                ItemDetailScreen(
                    onEdit = {
                        navController.navigate(Routes.itemEdit(UUID.fromString(itemId)))
                    },
                    onEditPlan = {
                        navController.navigate(Routes.itemPlan(UUID.fromString(itemId)))
                    },
                    onViewHistory = {
                        navController.navigate(Routes.itemHistory(UUID.fromString(itemId)))
                    },
                    onPurchased = {
                        val detailDestinationId = navController.currentDestination?.id
                        if (detailDestinationId != null) {
                            navController.navigate(Routes.archiveDetail(UUID.fromString(itemId))) {
                                popUpTo(detailDestinationId) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.archiveDetail(UUID.fromString(itemId)))
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.ITEM_HISTORY,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                ),
            ) {
                TransactionHistoryScreen()
            }

            composable(
                route = Routes.ITEM_PLAN,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                ),
            ) {
                SavingsPlanScreen(
                    onFinished = { navController.popBackStack() },
                )
            }

            composable(Routes.ARCHIVE) {
                ArchiveScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Routes.archiveDetail(itemId))
                    },
                )
            }

            composable(
                route = Routes.ARCHIVE_DETAIL,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                ),
            ) {
                ArchivedItemDetailScreen(
                    onFinished = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.ITEM_ADD,
                arguments = listOf(
                    navArgument("collectionId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                ItemEditorScreen(
                    onSaved = { itemId ->
                        val editorDestinationId = navController.currentDestination?.id
                        if (editorDestinationId != null) {
                            navController.navigate(Routes.itemDetail(itemId)) {
                                popUpTo(editorDestinationId) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.itemDetail(itemId))
                        }
                    },
                )
            }

            composable(
                route = Routes.ITEM_EDIT,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                ),
            ) {
                ItemEditorScreen(
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}
