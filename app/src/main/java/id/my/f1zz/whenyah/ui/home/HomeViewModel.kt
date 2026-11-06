package id.my.f1zz.whenyah.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

enum class ItemSort {
    RECENT,
    NAME,
}

data class HomeUiState(
    val items: List<WishlistItemEntity> = emptyList(),
    val collectionNames: Map<UUID, String> = emptyMap(),
    val query: String = "",
    val sort: ItemSort = ItemSort.RECENT,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: WishlistRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(ItemSort.RECENT)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeActiveItems(),
        repository.observeCollections(),
        query,
        sort,
    ) { items, collections, currentQuery, currentSort ->
        val collectionNames = collections.associate { it.collection.id to it.collection.name }
        val filtered = if (currentQuery.isBlank()) {
            items
        } else {
            items.filter { it.name.contains(currentQuery, ignoreCase = true) }
        }
        val sorted = when (currentSort) {
            ItemSort.RECENT -> filtered.sortedByDescending { it.updatedAt }
            ItemSort.NAME -> filtered.sortedBy { it.name.lowercase() }
        }

        HomeUiState(
            items = sorted,
            collectionNames = collectionNames,
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
