package id.my.f1zz.whenyah.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.model.CollectionWithItemCount
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.domain.dashboard.DashboardCalculator
import id.my.f1zz.whenyah.domain.dashboard.DashboardItem
import id.my.f1zz.whenyah.domain.dashboard.DashboardSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CollectionsUiState(
    val collections: List<CollectionWithItemCount> = emptyList(),
    val totals: Map<UUID, DashboardSummary> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: WishlistRepository,
) : ViewModel() {
    val uiState: StateFlow<CollectionsUiState> = combine(
        repository.observeCollections(),
        repository.observeActiveItemsWithSavings(),
    ) { collections, items ->
        val byCollection = items
            .mapNotNull { entry ->
                entry.item.collectionId?.let { collectionId -> collectionId to entry }
            }
            .groupBy({ it.first }, { it.second })
        val totals = byCollection.mapValues { (_, entries) ->
            DashboardCalculator.summarize(
                entries.map {
                    DashboardItem(
                        targetPriceIdr = it.item.targetPriceIdr,
                        availableIdr = it.savings.availableIdr,
                    )
                }
            )
        }
        CollectionsUiState(
            collections = collections,
            totals = totals,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionsUiState(),
    )

    fun createCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createCollection(name)
        }
    }

    fun renameCollection(collectionId: UUID, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.renameCollection(collectionId, name)
        }
    }

    fun deleteCollection(collectionId: UUID) {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
        }
    }
}
