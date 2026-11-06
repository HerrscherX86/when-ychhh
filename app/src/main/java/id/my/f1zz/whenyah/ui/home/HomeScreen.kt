package id.my.f1zz.whenyah.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.repository.WishlistItemWithSavings
import id.my.f1zz.whenyah.domain.dashboard.DashboardSummary
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.ItemCard
import id.my.f1zz.whenyah.ui.components.SavingsEventRow
import id.my.f1zz.whenyah.ui.util.formatDate
import id.my.f1zz.whenyah.ui.util.formatIdr
import id.my.f1zz.whenyah.ui.util.formatPlanFrequency
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onAddItem: () -> Unit,
    onItemClick: (UUID) -> Unit,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    DashboardSummaryCard(
                        summary = state.summary,
                        onOpenArchive = onOpenArchive,
                    )
                }

                if (state.recentActivity.isNotEmpty()) {
                    item { SectionHeader("Recent activity") }
                    items(items = state.recentActivity, key = { "activity-${it.event.id}" }) { activity ->
                        SavingsEventRow(
                            event = activity.event,
                            itemName = activity.itemName,
                        )
                    }
                }

                if (state.upcomingPlans.isNotEmpty()) {
                    item { SectionHeader("Upcoming plans") }
                    items(items = state.upcomingPlans, key = { "plan-${it.plan.id}" }) { upcoming ->
                        UpcomingPlanCard(upcoming)
                    }
                }

                if (state.nearestGoals.isNotEmpty()) {
                    item { SectionHeader("Nearest goals") }
                    items(items = state.nearestGoals, key = { "nearest-${it.item.id}" }) { entry ->
                        NearestGoalCard(
                            entry = entry,
                            onClick = { onItemClick(entry.item.id) },
                        )
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "All items",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search items") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                        },
                        singleLine = true,
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.sort == ItemSort.RECENT,
                            onClick = { viewModel.onSortChange(ItemSort.RECENT) },
                            label = { Text("Recent") },
                        )
                        FilterChip(
                            selected = state.sort == ItemSort.NAME,
                            onClick = { viewModel.onSortChange(ItemSort.NAME) },
                            label = { Text("Name") },
                        )
                    }
                }

                if (state.items.isEmpty()) {
                    item {
                        EmptyState(
                            title = if (state.query.isBlank()) {
                                "No items yet"
                            } else {
                                "No matching items"
                            },
                            message = if (state.query.isBlank()) {
                                "Tap + to add your first wishlist item."
                            } else {
                                "Try a different search."
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
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddItem,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add item")
        }
    }
}

@Composable
private fun DashboardSummaryCard(
    summary: DashboardSummary,
    onOpenArchive: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onOpenArchive) {
                    Text("Purchased archive")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryMetric(
                    label = "Active items",
                    value = summary.activeItemCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    label = "Total target",
                    value = formatIdr(summary.totalTargetIdr),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryMetric(
                    label = "Total saved",
                    value = formatIdr(summary.totalAvailableIdr),
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    label = "Fully funded",
                    value = "${summary.fullyFundedCount}/${summary.activeItemCount}",
                    modifier = Modifier.weight(1f),
                )
            }

            LinearProgressIndicator(
                progress = { summary.overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
            Text(
                text = when {
                    summary.activeItemCount == 0 -> "No active items yet"
                    summary.totalTargetIdr == 0L -> "No target amounts; all goals are satisfied"
                    summary.overallProgress >= 1f -> "All active goals funded"
                    else -> "${(summary.overallProgress * 100).roundToInt()}% of active targets funded"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun UpcomingPlanCard(upcoming: DashboardUpcomingPlan) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = upcoming.itemName,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${formatPlanFrequency(upcoming.plan.frequency)} • " +
                    formatIdr(upcoming.plan.contributionIdr),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val projection = upcoming.projection
            when {
                projection == null -> {
                    Text(
                        text = "No estimate available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                projection.isGoalReached -> {
                    Text(
                        text = "Goal reached",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                projection.completionDate == null -> {
                    Text(
                        text = "Projected date is too far in the future.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                else -> {
                    Text(
                        text = "${projection.requiredContributions} contributions • " +
                            "est. ${formatDate(projection.completionDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = "Planned, not paid",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NearestGoalCard(
    entry: WishlistItemWithSavings,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = entry.item.name,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${formatIdr(entry.savings.availableIdr)} of " +
                    formatIdr(entry.item.targetPriceIdr),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { entry.savings.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            )
            Text(
                text = "${formatIdr(entry.savings.remainingIdr)} remaining",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
