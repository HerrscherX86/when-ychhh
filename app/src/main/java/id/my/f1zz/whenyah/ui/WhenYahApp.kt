package id.my.f1zz.whenyah.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.navigation.AppLauncher
import id.my.f1zz.whenyah.data.navigation.LaunchRequest
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.navigation.Routes
import id.my.f1zz.whenyah.navigation.TopLevelDestination
import id.my.f1zz.whenyah.ui.archive.ArchiveScreen
import id.my.f1zz.whenyah.ui.archive.ArchivedItemDetailScreen
import id.my.f1zz.whenyah.ui.collections.CollectionDetailScreen
import id.my.f1zz.whenyah.ui.collections.CollectionsScreen
import id.my.f1zz.whenyah.ui.components.AppFeedback
import id.my.f1zz.whenyah.ui.components.LocalAppFeedback
import id.my.f1zz.whenyah.ui.home.ActivityScreen
import id.my.f1zz.whenyah.ui.home.HomeScreen
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.item.ItemDetailScreen
import id.my.f1zz.whenyah.ui.navigation.LocalSetTopBarBackHandler
import id.my.f1zz.whenyah.ui.item.ItemEditorScreen
import id.my.f1zz.whenyah.ui.item.SavingsPlanScreen
import id.my.f1zz.whenyah.ui.item.TransactionHistoryScreen
import id.my.f1zz.whenyah.ui.settings.SettingsScreen
import id.my.f1zz.whenyah.ui.theme.AppMotion
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhenYahApp(launcher: AppLauncher) {
    val strings = LocalAppStrings.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentTopLevelDestination = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }
    val isTopLevel = currentTopLevelDestination != null
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(600)
    val launchRequest by launcher.request.collectAsStateWithLifecycle()
    var topBarBackHandler by remember { mutableStateOf<(() -> Unit)?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val feedbackScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val appFeedback = remember(snackbarHostState, feedbackScope, haptics) {
        AppFeedback(snackbarHostState, feedbackScope, haptics)
    }

    val tabEnter = fadeIn(
        animationSpec = tween(
            durationMillis = AppMotion.TabFadeInMillis,
            delayMillis = AppMotion.TabFadeOutMillis,
            easing = AppMotion.FadeInEasing,
        )
    )
    val tabExit = fadeOut(
        animationSpec = tween(
            durationMillis = AppMotion.TabFadeOutMillis,
            easing = AppMotion.FadeOutEasing,
        )
    )
    val detailFadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = AppMotion.FadeInMillis,
            delayMillis = AppMotion.FadeOutMillis,
            easing = AppMotion.FadeInEasing,
        )
    )
    val detailFadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = AppMotion.FadeOutMillis,
            easing = AppMotion.FadeOutEasing,
        )
    )

    val title = when (currentRoute) {
        Routes.ITEM_DETAIL -> strings.titleItemDetails
        Routes.ITEM_HISTORY -> strings.titleTransactionHistory
        Routes.ITEM_PLAN -> strings.titleSavingsPlan
        Routes.ITEM_ADD -> strings.titleAddItem
        Routes.ITEM_EDIT -> strings.titleEditItem
        Routes.COLLECTION_DETAIL -> strings.titleCollection
        Routes.ARCHIVE -> strings.titlePurchasedArchive
        Routes.ARCHIVE_DETAIL -> strings.titleArchivedItem
        Routes.ACTIVITY -> strings.titleActivity
        else -> currentTopLevelDestination?.label(strings) ?: strings.appName
    }

    LaunchedEffect(launchRequest) {
        when (val request = launchRequest) {
            is LaunchRequest.OpenItem -> navController.navigate(
                Routes.itemDetail(request.itemId, request.depositAmountIdr)
            ) {
                launchSingleTop = true
            }

            LaunchRequest.AddItem -> navController.navigate(Routes.itemAdd()) {
                launchSingleTop = true
            }

            null -> Unit
        }
        if (launchRequest != null) {
            launcher.consume()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(
                            onClick = {
                                topBarBackHandler?.invoke() ?: navController.navigateUp()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.back,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isTopLevel && !isExpanded) {
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
                            label = {
                                Text(
                                    text = destination.label(strings),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalAppFeedback provides appFeedback,
            LocalSetTopBarBackHandler provides { handler -> topBarBackHandler = handler },
        ) {
            Row(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                if (isExpanded && isTopLevel) {
                    NavigationRail {
                        TopLevelDestination.entries.forEach { destination ->
                            NavigationRailItem(
                                selected = currentTopLevelDestination == destination,
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
                                label = {
                                    Text(
                                        text = destination.label(strings),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                }
                NavHost(
                    navController = navController,
                    startDestination = TopLevelDestination.HOME.route,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    enterTransition = { detailFadeIn },
                    exitTransition = { detailFadeOut },
                    popEnterTransition = { detailFadeIn },
                    popExitTransition = { detailFadeOut },
                ) {
            composable(
                route = TopLevelDestination.HOME.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit },
            ) {
                HomeScreen(
                    onAddItem = { navController.navigate(Routes.itemAdd()) },
                    onItemClick = { itemId ->
                        navController.navigate(Routes.itemDetail(itemId))
                    },
                    onPlanClick = { itemId ->
                        navController.navigate(Routes.itemPlan(itemId))
                    },
                    onViewAllActivity = { navController.navigate(Routes.ACTIVITY) },
                    onOpenArchive = { navController.navigate(Routes.ARCHIVE) },
                )
            }
            composable(
                route = TopLevelDestination.COLLECTIONS.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit },
            ) {
                CollectionsScreen(
                    onCollectionClick = { collectionId ->
                        navController.navigate(Routes.collectionDetail(collectionId))
                    },
                )
            }
            composable(
                route = TopLevelDestination.SETTINGS.route,
                enterTransition = { tabEnter },
                exitTransition = { tabExit },
                popEnterTransition = { tabEnter },
                popExitTransition = { tabExit },
            ) {
                SettingsScreen(
                    onOpenArchive = { navController.navigate(Routes.ARCHIVE) },
                )
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
                    navArgument("depositAmount") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
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

            composable(Routes.ACTIVITY) {
                ActivityScreen()
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
                    onBack = { navController.popBackStack() },
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
                    onBack = { navController.popBackStack() },
                )
            }
            }
            }
        }
    }
}
private fun TopLevelDestination.label(strings: AppStrings): String = when (this) {
    TopLevelDestination.HOME -> strings.navHome
    TopLevelDestination.COLLECTIONS -> strings.navCollections
    TopLevelDestination.SETTINGS -> strings.navSettings
}
