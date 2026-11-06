package id.my.f1zz.whenyah.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.data.repository.SavingsPlanRepository
import id.my.f1zz.whenyah.data.repository.SavingsRepository
import id.my.f1zz.whenyah.data.repository.WishlistItemWithSavings
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.domain.dashboard.DashboardCalculator
import id.my.f1zz.whenyah.domain.dashboard.DashboardItem
import id.my.f1zz.whenyah.domain.dashboard.DashboardSummary
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.plan.SavingsPlanCalculator
import id.my.f1zz.whenyah.domain.plan.SavingsPlanProjection
import id.my.f1zz.whenyah.domain.plan.observeToday
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

enum class ItemSort {
    RECENT,
    NAME,
}

data class DashboardActivity(
    val event: SavingsEventEntity,
    val itemName: String,
)

data class DashboardUpcomingPlan(
    val plan: SavingsPlanEntity,
    val itemName: String,
    val projection: SavingsPlanProjection?,
)

data class HomeUiState(
    val items: List<WishlistItemWithSavings> = emptyList(),
    val collectionNames: Map<UUID, String> = emptyMap(),
    val summary: DashboardSummary = DashboardSummary.Empty,
    val recentActivity: List<DashboardActivity> = emptyList(),
    val upcomingPlans: List<DashboardUpcomingPlan> = emptyList(),
    val nearestGoals: List<WishlistItemWithSavings> = emptyList(),
    val query: String = "",
    val sort: ItemSort = ItemSort.RECENT,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: WishlistRepository,
    savingsRepository: SavingsRepository,
    savingsPlanRepository: SavingsPlanRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(ItemSort.RECENT)

    private val dashboardData = combine(
        savingsRepository.observeAllEvents(),
        savingsPlanRepository.observeActivePlans(),
        observeToday(),
    ) { events, plans, today -> Triple(events, plans, today) }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeActiveItemsWithSavings(),
        repository.observeCollections(),
        dashboardData,
        query,
        sort,
    ) { items, collections, extra, currentQuery, currentSort ->
        val (events, plans, today) = extra
        val collectionNames = collections.associate { it.collection.id to it.collection.name }
        val itemsById = items.associateBy { it.item.id }

        val summary = DashboardCalculator.summarize(
            items.map {
                DashboardItem(
                    targetPriceIdr = it.item.targetPriceIdr,
                    availableIdr = it.savings.availableIdr,
                )
            }
        )

        val recentActivity = events
            .asSequence()
            .filter {
                it.type == SavingsEventType.DEPOSIT ||
                    it.type == SavingsEventType.WITHDRAWAL
            }
            .mapNotNull { event ->
                itemsById[event.itemId]?.let { entry ->
                    DashboardActivity(event = event, itemName = entry.item.name)
                }
            }
            .sortedByDescending { it.event.createdAt }
            .take(5)
            .toList()

        val upcomingPlans = plans
            .asSequence()
            .mapNotNull { plan ->
                val entry = itemsById[plan.itemId] ?: return@mapNotNull null
                val projection = runCatching {
                    SavingsPlanCalculator.project(
                        targetPriceIdr = entry.item.targetPriceIdr,
                        availableBalanceIdr = entry.savings.availableIdr,
                        contributionIdr = plan.contributionIdr,
                        frequency = plan.frequency,
                        startDate = today,
                    )
                }.getOrNull()

                DashboardUpcomingPlan(
                    plan = plan,
                    itemName = entry.item.name,
                    projection = projection,
                )
            }
            .sortedWith(
                compareBy<DashboardUpcomingPlan, LocalDate?>(nullsLast()) {
                    it.projection?.completionDate
                }
            )
            .take(3)
            .toList()

        val nearestGoals = items
            .filter { !it.savings.isGoalReached }
            .sortedBy { it.savings.remainingIdr }
            .take(5)

        val filtered = if (currentQuery.isBlank()) {
            items
        } else {
            items.filter { it.item.name.contains(currentQuery, ignoreCase = true) }
        }
        val sorted = when (currentSort) {
            ItemSort.RECENT -> filtered.sortedByDescending { it.item.updatedAt }
            ItemSort.NAME -> filtered.sortedBy { it.item.name.lowercase() }
        }

        HomeUiState(
            items = sorted,
            collectionNames = collectionNames,
            summary = summary,
            recentActivity = recentActivity,
            upcomingPlans = upcomingPlans,
            nearestGoals = nearestGoals,
            query = currentQuery,
            sort = currentSort,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onSortChange(value: ItemSort) {
        sort.value = value
    }
}
