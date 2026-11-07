package id.my.f1zz.whenyah.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.repository.WishlistItemWithSavings
import id.my.f1zz.whenyah.domain.dashboard.DashboardSummary
import id.my.f1zz.whenyah.domain.model.SavingsEventType
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
    val listState = rememberLazyListState()
    val fabVisible by rememberFabVisibility(listState)

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    OverviewCard(
                        summary = state.summary,
                        onOpenArchive = onOpenArchive,
                    )
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

                if (state.upcomingPlans.isNotEmpty()) {
                    item { SectionHeader(title = strings.upcomingPlans) }
                    items(
                        items = state.upcomingPlans,
                        key = { "plan-${it.plan.id}" },
                    ) { upcoming ->
                        UpcomingPlanCard(
                            upcoming = upcoming,
                            onClick = { onPlanClick(upcoming.plan.itemId) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                if (state.nearestGoals.isNotEmpty()) {
                    item { SectionHeader(title = strings.closestGoals) }
                    item {
                        NearestGoalsRow(
                            goals = state.nearestGoals,
                            onItemClick = onItemClick,
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = strings.allItems,
                        action = { CountBadge(state.summary.activeItemCount) },
                    )
                }

                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
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
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.sort == ItemSort.RECENT,
                            onClick = { viewModel.onSortChange(ItemSort.RECENT) },
                            label = { Text(strings.sortRecent) },
                        )
                        FilterChip(
                            selected = state.sort == ItemSort.NAME,
                            onClick = { viewModel.onSortChange(ItemSort.NAME) },
                            label = { Text(strings.sortName) },
                        )
                    }
                }

                if (state.items.isEmpty()) {
                    item {
                        EmptyState(
                            title = if (state.query.isBlank()) {
                                strings.noItemsYet
                            } else {
                                strings.noMatchingItems
                            },
                            message = if (state.query.isBlank()) {
                                strings.noItemsMessage
                            } else {
                                strings.noMatchingMessage
                            },
                        )
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

        ScrollAwareAddFab(
            visible = fabVisible,
            onClick = onAddItem,
            contentDescription = strings.addItemDescription,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

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
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OverviewMetric(
                    label = strings.activeItems,
                    value = summary.activeItemCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                OverviewMetric(
                    label = strings.totalTarget,
                    value = formatIdr(summary.totalTargetIdr),
                    modifier = Modifier.weight(1f),
                )
                OverviewMetric(
                    label = strings.fullyFundedLabel,
                    value = "${summary.fullyFundedCount}/${summary.activeItemCount}",
                    modifier = Modifier.weight(1f),
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
private fun NearestGoalsRow(
    goals: List<WishlistItemWithSavings>,
    onItemClick: (UUID) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = goals, key = { "nearest-${it.item.id}" }) { entry ->
            NearestGoalCard(
                entry = entry,
                onClick = { onItemClick(entry.item.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun NearestGoalCard(
    entry: WishlistItemWithSavings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val animatedProgress by animateFloatAsState(
        targetValue = entry.savings.progress,
        animationSpec = tween(
            durationMillis = AppMotion.SlowMillis,
            easing = AppMotion.StandardEasing,
        ),
        label = "nearestGoalProgress",
    )
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
                text = strings.amountLeft(formatIdr(entry.savings.remainingIdr)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
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
