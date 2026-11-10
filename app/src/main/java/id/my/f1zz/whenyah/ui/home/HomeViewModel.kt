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
import id.my.f1zz.whenyah.domain.wishlist.GoalFilter
import id.my.f1zz.whenyah.domain.wishlist.ItemSortOrder
import id.my.f1zz.whenyah.domain.wishlist.PlanFilter
import id.my.f1zz.whenyah.domain.wishlist.WishlistItemFacts
import id.my.f1zz.whenyah.domain.wishlist.WishlistQuery
import id.my.f1zz.whenyah.domain.wishlist.WishlistQueryEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

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
    val readyToBuy: List<WishlistItemWithSavings> = emptyList(),
    val queryText: String = "",
    val sort: ItemSortOrder = ItemSortOrder.RECENT,
    val collectionId: UUID? = null,
    val category: String? = null,
    val goal: GoalFilter = GoalFilter.ALL,
    val plan: PlanFilter = PlanFilter.ALL,
    val isFiltering: Boolean = false,
    val collectionOptions: List<Pair<UUID, String>> = emptyList(),
    val categoryOptions: List<String> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: WishlistRepository,
    savingsRepository: SavingsRepository,
    savingsPlanRepository: SavingsPlanRepository,
) : ViewModel() {
    private val query = MutableStateFlow(WishlistQuery())

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
    ) { items, collections, extra, currentQuery ->
        val (events, plans, today) = extra
        val collectionNames = collections.associate { it.collection.id to it.collection.name }
        val itemsById = items.associateBy { it.item.id }
        val plannedItemIds = plans.mapTo(mutableSetOf()) { it.itemId }

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
            .take(3)
            .toList()

        val upcomingPlans = plans
            .asSequence()
            .mapNotNull { plan ->
                val entry = itemsById[plan.itemId] ?: return@mapNotNull null
                if (entry.savings.isGoalReached) return@mapNotNull null

                val projection = runCatching {
                    SavingsPlanCalculator.project(
                        targetPriceIdr = entry.item.targetPriceIdr,
                        availableBalanceIdr = entry.savings.availableIdr,
                        contributionIdr = plan.contributionIdr,
                        frequency = plan.frequency,
                        startDate = today,
                    )
                }.getOrNull()
                if (projection?.isGoalReached == true) return@mapNotNull null

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

        val readyToBuy = items
            .filter { it.savings.isGoalReached && it.item.targetPriceIdr > 0 }
            .sortedBy { it.item.name.lowercase() }
            .take(5)

        val matches = WishlistQueryEngine.applyQuery(
            items = items.map { it.toFacts(hasPlan = it.item.id in plannedItemIds) },
            query = currentQuery,
        )
        val ordered = matches.mapNotNull { facts -> itemsById[facts.id] }

        val categoryOptions = items
            .mapNotNull { it.item.category?.trim()?.takeIf(String::isNotBlank) }
            .distinct()
            .sorted()

        HomeUiState(
            items = ordered,
            collectionNames = collectionNames,
            summary = summary,
            recentActivity = recentActivity,
            upcomingPlans = upcomingPlans,
            nearestGoals = nearestGoals,
            readyToBuy = readyToBuy,
            queryText = currentQuery.text,
            sort = currentQuery.sort,
            collectionId = currentQuery.collectionId,
            category = currentQuery.category,
            goal = currentQuery.goal,
            plan = currentQuery.plan,
            isFiltering = currentQuery.isFiltering,
            collectionOptions = collections.map { it.collection.id to it.collection.name },
            categoryOptions = categoryOptions,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onQueryChange(value: String) {
        query.update { it.copy(text = value) }
    }

    fun onSortChange(value: ItemSortOrder) {
        query.update { it.copy(sort = value) }
    }

    fun onCollectionFilterChange(value: UUID?) {
        query.update { it.copy(collectionId = value) }
    }

    fun onCategoryFilterChange(value: String?) {
        query.update { it.copy(category = value) }
    }

    fun onGoalFilterChange(value: GoalFilter) {
        query.update { it.copy(goal = value) }
    }

    fun onPlanFilterChange(value: PlanFilter) {
        query.update { it.copy(plan = value) }
    }

    fun clearFilters() {
        query.update {
            it.copy(
                collectionId = null,
                category = null,
                goal = GoalFilter.ALL,
                plan = PlanFilter.ALL,
            )
        }
    }

    private fun WishlistItemWithSavings.toFacts(hasPlan: Boolean) = WishlistItemFacts(
        id = item.id,
        name = item.name,
        updatedAt = item.updatedAt,
        targetPriceIdr = item.targetPriceIdr,
        availableIdr = savings.availableIdr,
        progress = savings.progress,
        isGoalReached = savings.isGoalReached,
        collectionId = item.collectionId,
        category = item.category,
        hasPlan = hasPlan,
    )
}
