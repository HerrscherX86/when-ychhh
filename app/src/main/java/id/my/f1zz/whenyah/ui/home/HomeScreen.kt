package id.my.f1zz.whenyah.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.repository.WishlistItemWithSavings
import id.my.f1zz.whenyah.domain.dashboard.DashboardSummary
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.wishlist.GoalFilter
import id.my.f1zz.whenyah.domain.wishlist.ItemSortOrder
import id.my.f1zz.whenyah.domain.wishlist.PlanFilter
import id.my.f1zz.whenyah.ui.components.AdaptiveContent
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.ItemCard
import id.my.f1zz.whenyah.ui.components.ScrollAwareAddFab
import id.my.f1zz.whenyah.ui.components.rememberFabVisibility
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.AppMotion
import id.my.f1zz.whenyah.ui.util.formatDate
import id.my.f1zz.whenyah.ui.util.formatIdr
import id.my.f1zz.whenyah.ui.util.formatShortDate
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddItem: () -> Unit,
    onItemClick: (UUID) -> Unit,
    onPlanClick: (UUID) -> Unit,
    onViewAllActivity: () -> Unit,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.OVERVIEW) }
    val overviewListState = rememberLazyListState()
    val itemsListState = rememberLazyListState()
    val overviewFabVisible by rememberFabVisibility(overviewListState)
    val itemsFabVisible by rememberFabVisibility(itemsListState)
    var searchFocused by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    val fabVisible = when (selectedTab) {
        HomeTab.OVERVIEW -> overviewFabVisible
        HomeTab.ITEMS -> itemsFabVisible
    }

    Box(modifier = modifier.fillMaxSize()) {
        AdaptiveContent(maxWidth = 960.dp) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeTabSwitcher(
                    selectedTab = selectedTab,
                    strings = strings,
                    onSelect = { tab ->
                        selectedTab = tab
                        if (tab == HomeTab.OVERVIEW) {
                            searchFocused = false
                        }
                    },
                )

                when (selectedTab) {
                    HomeTab.OVERVIEW -> OverviewTab(
                        state = state,
                        strings = strings,
                        listState = overviewListState,
                        onAddItem = onAddItem,
                        onItemClick = onItemClick,
                        onPlanClick = onPlanClick,
                        onViewAllActivity = onViewAllActivity,
                        onOpenArchive = onOpenArchive,
                        onBrowseAllItems = { selectedTab = HomeTab.ITEMS },
                    )

                    HomeTab.ITEMS -> ItemsTab(
                        state = state,
                        strings = strings,
                        listState = itemsListState,
                        onQueryChange = viewModel::onQueryChange,
                        onSearchFocusChanged = { searchFocused = it },
                        onFilterClick = { showFilters = true },
                        onAddItem = onAddItem,
                        onItemClick = onItemClick,
                        onClearCollection = { viewModel.onCollectionFilterChange(null) },
                        onClearCategory = { viewModel.onCategoryFilterChange(null) },
                        onClearGoal = { viewModel.onGoalFilterChange(GoalFilter.ALL) },
                        onClearPlan = { viewModel.onPlanFilterChange(PlanFilter.ALL) },
                        onClearAll = viewModel::clearFilters,
                    )
                }
            }
        }

        ScrollAwareAddFab(
            visible = fabVisible && !(selectedTab == HomeTab.ITEMS && searchFocused),
            onClick = onAddItem,
            contentDescription = strings.addItemDescription,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }

    if (showFilters) {
        HomeFilterSheet(
            state = state,
            onDismiss = { showFilters = false },
            onSortChange = viewModel::onSortChange,
            onCollectionChange = viewModel::onCollectionFilterChange,
            onCategoryChange = viewModel::onCategoryFilterChange,
            onGoalChange = viewModel::onGoalFilterChange,
            onPlanChange = viewModel::onPlanFilterChange,
            onClearAll = viewModel::clearFilters,
        )
    }
}

private enum class HomeTab {
    OVERVIEW,
    ITEMS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTabSwitcher(
    selectedTab: HomeTab,
    strings: AppStrings,
    onSelect: (HomeTab) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
    ) {
        HomeTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onSelect(tab) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = HomeTab.entries.size,
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = when (tab) {
                        HomeTab.OVERVIEW -> strings.overviewTab
                        HomeTab.ITEMS -> strings.itemsTab
                    },
                )
            }
        }
    }
}

@Composable
private fun OverviewTab(
    state: HomeUiState,
    strings: AppStrings,
    listState: LazyListState,
    onAddItem: () -> Unit,
    onItemClick: (UUID) -> Unit,
    onPlanClick: (UUID) -> Unit,
    onViewAllActivity: () -> Unit,
    onOpenArchive: () -> Unit,
    onBrowseAllItems: () -> Unit,
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 140.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.items.isEmpty()) {
            item {
                EmptyState(
                    title = strings.noItemsYet,
                    message = strings.noItemsMessage,
                )
            }
            item {
                OutlinedButton(
                    onClick = onAddItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(strings.addItem)
                }
            }
        } else {
            item {
                OverviewCard(
                    summary = state.summary,
                    onOpenArchive = onOpenArchive,
                )
            }

            val nextPlan = state.upcomingPlans.firstOrNull()
            val nextGoal = state.nearestGoals.firstOrNull()
            if (nextPlan != null) {
                item { SectionHeader(title = strings.nextUp) }
                item {
                    UpcomingPlanCard(
                        upcoming = nextPlan,
                        onClick = { onPlanClick(nextPlan.plan.itemId) },
                    )
                }
            } else if (nextGoal != null) {
                item { SectionHeader(title = strings.nextUp) }
                item {
                    ItemCard(
                        item = nextGoal.item,
                        savings = nextGoal.savings,
                        collectionName = state.collectionNames[nextGoal.item.collectionId],
                        onClick = { onItemClick(nextGoal.item.id) },
                    )
                }
            }

            if (state.readyToBuy.isNotEmpty()) {
                item { SectionHeader(title = strings.readyToBuy) }
                item {
                    ReadyToBuyRow(
                        goals = state.readyToBuy,
                        onItemClick = onItemClick,
                    )
                }
            }

            if (state.recentActivity.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = strings.recentActivity,
                        action = {
                            TextButton(onClick = onViewAllActivity) {
                                Text(strings.viewAll)
                            }
                        },
                    )
                }
                item {
                    RecentActivityCard(activities = state.recentActivity)
                }
            }

            item {
                OutlinedButton(
                    onClick = onBrowseAllItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(strings.allItems)
                }
            }
        }
    }
}

@Composable
private fun ItemsTab(
    state: HomeUiState,
    strings: AppStrings,
    listState: LazyListState,
    onQueryChange: (String) -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit,
    onFilterClick: () -> Unit,
    onAddItem: () -> Unit,
    onItemClick: (UUID) -> Unit,
    onClearCollection: () -> Unit,
    onClearCategory: () -> Unit,
    onClearGoal: () -> Unit,
    onClearPlan: () -> Unit,
    onClearAll: () -> Unit,
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(
        state.queryText,
        state.sort,
        state.collectionId,
        state.category,
        state.goal,
        state.plan,
    ) {
        if (listState.firstVisibleItemIndex > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.queryText,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onSearchFocusChanged(it.isFocused) },
                placeholder = { Text(strings.searchItems) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
            FilledTonalIconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = strings.filterAction,
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 4.dp,
                bottom = 140.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionHeader(
                    title = strings.allItems,
                    action = { CountBadge(state.items.size) },
                )
            }

            if (state.isFiltering) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AppliedFilterChips(
                            state = state,
                            strings = strings,
                            onClearCollection = onClearCollection,
                            onClearCategory = onClearCategory,
                            onClearGoal = onClearGoal,
                            onClearPlan = onClearPlan,
                        )
                        TextButton(onClick = onClearAll) {
                            Text(strings.filterClearAll)
                        }
                    }
                }
            }

            if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        title = when {
                            state.isFiltering -> strings.noFilteredItems
                            state.queryText.isNotBlank() -> strings.noMatchingItems
                            else -> strings.noItemsYet
                        },
                        message = when {
                            state.isFiltering -> strings.noFilteredItemsMessage
                            state.queryText.isNotBlank() -> strings.noMatchingMessage
                            else -> strings.noItemsMessage
                        },
                    )
                }
                if (!state.isFiltering && state.queryText.isBlank()) {
                    item {
                        OutlinedButton(
                            onClick = onAddItem,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text(strings.addItem)
                        }
                    }
                }
            } else {
                items(items = state.items, key = { "item-${it.item.id}" }) { entry ->
                    ItemCard(
                        item = entry.item,
                        savings = entry.savings,
                        collectionName = state.collectionNames[entry.item.collectionId],
                        onClick = { onItemClick(entry.item.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppliedFilterChips(
    state: HomeUiState,
    strings: AppStrings,
    onClearCollection: () -> Unit,
    onClearCategory: () -> Unit,
    onClearGoal: () -> Unit,
    onClearPlan: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.collectionId?.let { id ->
            RemovableFilterChip(
                label = state.collectionNames[id] ?: strings.filterCollection,
                contentDescription = strings.clearFilterDescription,
                onRemove = onClearCollection,
            )
        }
        state.category?.let { category ->
            RemovableFilterChip(
                label = category,
                contentDescription = strings.clearFilterDescription,
                onRemove = onClearCategory,
            )
        }
        if (state.goal != GoalFilter.ALL) {
            RemovableFilterChip(
                label = when (state.goal) {
                    GoalFilter.REACHED -> strings.filterGoalReached
                    GoalFilter.NOT_REACHED -> strings.filterStillSaving
                    GoalFilter.ALL -> strings.filterAll
                },
                contentDescription = strings.clearFilterDescription,
                onRemove = onClearGoal,
            )
        }
        if (state.plan != PlanFilter.ALL) {
            RemovableFilterChip(
                label = when (state.plan) {
                    PlanFilter.HAS_PLAN -> strings.filterHasPlan
                    PlanFilter.NO_PLAN -> strings.filterNoPlan
                    PlanFilter.ALL -> strings.filterAll
                },
                contentDescription = strings.clearFilterDescription,
                onRemove = onClearPlan,
            )
        }
    }
}

@Composable
private fun RemovableFilterChip(
    label: String,
    contentDescription: String,
    onRemove: () -> Unit,
) {
    FilterChip(
        selected = true,
        onClick = onRemove,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFilterSheet(
    state: HomeUiState,
    onDismiss: () -> Unit,
    onSortChange: (ItemSortOrder) -> Unit,
    onCollectionChange: (UUID?) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onGoalChange: (GoalFilter) -> Unit,
    onPlanChange: (PlanFilter) -> Unit,
    onClearAll: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = strings.filtersTitle,
                style = MaterialTheme.typography.headlineSmall,
            )

            FilterGroup(title = strings.sortBy) {
                ItemSortOrder.entries.forEach { sort ->
                    FilterChip(
                        selected = state.sort == sort,
                        onClick = { onSortChange(sort) },
                        label = { Text(sortLabel(sort, strings)) },
                    )
                }
            }

            FilterGroup(title = strings.filterStatus) {
                FilterChip(
                    selected = state.goal == GoalFilter.ALL,
                    onClick = { onGoalChange(GoalFilter.ALL) },
                    label = { Text(strings.filterAll) },
                )
                FilterChip(
                    selected = state.goal == GoalFilter.REACHED,
                    onClick = { onGoalChange(GoalFilter.REACHED) },
                    label = { Text(strings.filterGoalReached) },
                )
                FilterChip(
                    selected = state.goal == GoalFilter.NOT_REACHED,
                    onClick = { onGoalChange(GoalFilter.NOT_REACHED) },
                    label = { Text(strings.filterStillSaving) },
                )
            }

            FilterGroup(title = strings.filterPlan) {
                FilterChip(
                    selected = state.plan == PlanFilter.ALL,
                    onClick = { onPlanChange(PlanFilter.ALL) },
                    label = { Text(strings.filterAll) },
                )
                FilterChip(
                    selected = state.plan == PlanFilter.HAS_PLAN,
                    onClick = { onPlanChange(PlanFilter.HAS_PLAN) },
                    label = { Text(strings.filterHasPlan) },
                )
                FilterChip(
                    selected = state.plan == PlanFilter.NO_PLAN,
                    onClick = { onPlanChange(PlanFilter.NO_PLAN) },
                    label = { Text(strings.filterNoPlan) },
                )
            }

            if (state.categoryOptions.isNotEmpty()) {
                FilterGroup(title = strings.filterCategory) {
                    FilterChip(
                        selected = state.category == null,
                        onClick = { onCategoryChange(null) },
                        label = { Text(strings.filterAll) },
                    )
                    state.categoryOptions.forEach { category ->
                        FilterChip(
                            selected = state.category.equals(category, ignoreCase = true),
                            onClick = {
                                onCategoryChange(
                                    if (state.category.equals(category, ignoreCase = true)) {
                                        null
                                    } else {
                                        category
                                    }
                                )
                            },
                            label = { Text(category) },
                        )
                    }
                }
            }

            if (state.collectionOptions.isNotEmpty()) {
                FilterGroup(title = strings.filterCollection) {
                    FilterChip(
                        selected = state.collectionId == null,
                        onClick = { onCollectionChange(null) },
                        label = { Text(strings.filterAll) },
                    )
                    state.collectionOptions.forEach { (id, name) ->
                        FilterChip(
                            selected = state.collectionId == id,
                            onClick = {
                                onCollectionChange(if (state.collectionId == id) null else id)
                            },
                            label = { Text(name) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onClearAll,
                    enabled = state.isFiltering,
                ) {
                    Text(strings.filterClearAll)
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(strings.done)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewCard(
    summary: DashboardSummary,
    onOpenArchive: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val animatedProgress by animateFloatAsState(
        targetValue = summary.overallProgress,
        animationSpec = tween(
            durationMillis = AppMotion.SlowMillis,
            easing = AppMotion.StandardEasing,
        ),
        label = "overviewProgress",
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.totalSaved,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    )
                    Text(
                        text = formatIdr(summary.totalAvailableIdr),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                TextButton(
                    onClick = onOpenArchive,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.archiveShort)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .semantics {
                            contentDescription = strings.percentageFunded(
                                (summary.overallProgress * 100).roundToInt()
                            )
                        },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = progressLabel(summary, strings),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = strings.overallProgress,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OverviewMetric(
                    label = strings.activeItems,
                    value = summary.activeItemCount.toString(),
                )
                OverviewMetric(
                    label = strings.totalTarget,
                    value = formatIdr(summary.totalTargetIdr),
                )
                OverviewMetric(
                    label = strings.fullyFundedLabel,
                    value = "${summary.fullyFundedCount}/${summary.activeItemCount}",
                )
            }
        }
    }
}

@Composable
private fun OverviewMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        action?.invoke()
    }
}

@Composable
private fun CountBadge(count: Int) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun RecentActivityCard(activities: List<DashboardActivity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = AppMotion.MediumMillis,
                    easing = AppMotion.StandardEasing,
                )
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            activities.forEachIndexed { index, activity ->
                HomeActivityRow(activity = activity)
                if (index < activities.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeActivityRow(activity: DashboardActivity) {
    val strings = LocalAppStrings.current
    val event = activity.event
    val isDeposit = event.type == SavingsEventType.DEPOSIT
    val containerColor = if (isDeposit) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (isDeposit) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    val amountColor = if (isDeposit) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val label = if (isDeposit) strings.deposit else strings.withdrawal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
        ) {
            Icon(
                imageVector = if (isDeposit) Icons.Filled.Add else Icons.Filled.Remove,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.itemName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$label · ${formatShortDate(event.createdAt, strings.locale)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${if (isDeposit) "+" else "-"}${formatIdr(event.amountIdr)}",
            style = MaterialTheme.typography.titleSmall,
            color = amountColor,
        )
    }
}

@Composable
private fun UpcomingPlanCard(
    upcoming: DashboardUpcomingPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = upcoming.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatIdr(upcoming.plan.contributionIdr)} " +
                        strings.planFrequency(upcoming.plan.frequency).lowercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                val projection = upcoming.projection
                when {
                    projection == null -> {
                        Text(
                            text = strings.noEstimateAvailable,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    projection.isGoalReached -> {
                        Text(
                            text = strings.goalReached,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }

                    projection.completionDate == null -> {
                        Text(
                            text = strings.dateTooFar,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    else -> {
                        Text(
                            text = strings.contributionsEstimate(
                                projection.requiredContributions,
                                formatDate(projection.completionDate, strings.locale),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = strings.remainingLabel(formatIdr(projection.remainingIdr)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = strings.plannedNotPaid,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReadyToBuyRow(
    goals: List<WishlistItemWithSavings>,
    onItemClick: (UUID) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = goals, key = { "ready-${it.item.id}" }) { entry ->
            GoalCarouselCard(
                entry = entry,
                onClick = { onItemClick(entry.item.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun GoalCarouselCard(
    entry: WishlistItemWithSavings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isFunded = entry.savings.isGoalReached
    val animatedProgress by animateFloatAsState(
        targetValue = entry.savings.progress,
        animationSpec = tween(
            durationMillis = AppMotion.SlowMillis,
            easing = AppMotion.StandardEasing,
        ),
        label = "goalCarouselProgress",
    )
    val accentColor = if (isFunded) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    Card(
        onClick = onClick,
        modifier = modifier.width(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = entry.item.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (isFunded) {
                    strings.goalReached
                } else {
                    strings.amountLeft(formatIdr(entry.savings.remainingIdr))
                },
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .semantics {
                        contentDescription = strings.percentageFunded(
                            (entry.savings.progress * 100).roundToInt()
                        )
                    },
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = strings.percentageSaved((entry.savings.progress * 100).roundToInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatIdr(entry.savings.availableIdr),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun progressLabel(summary: DashboardSummary, strings: id.my.f1zz.whenyah.domain.i18n.AppStrings): String = when {
    summary.activeItemCount == 0 -> strings.noActiveItemsYet
    summary.totalTargetIdr == 0L -> strings.noTargetAmountSet
    summary.overallProgress >= 1f -> strings.allActiveGoalsFunded
    else -> strings.percentageFunded((summary.overallProgress * 100).roundToInt())
}

private fun sortLabel(sort: ItemSortOrder, strings: AppStrings): String = when (sort) {
    ItemSortOrder.RECENT -> strings.sortRecent
    ItemSortOrder.NAME -> strings.sortName
    ItemSortOrder.PROGRESS -> strings.sortProgress
    ItemSortOrder.TARGET_PRICE -> strings.sortPrice
}
