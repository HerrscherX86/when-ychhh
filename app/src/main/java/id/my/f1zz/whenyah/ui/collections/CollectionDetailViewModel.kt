package id.my.f1zz.whenyah.ui.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

data class CollectionDetailUiState(
    val collection: CollectionEntity? = null,
    val items: List<WishlistItemEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: WishlistRepository,
) : ViewModel() {
    private val collectionId = UUID.fromString(
        checkNotNull(savedStateHandle.get<String>("collectionId")) {
            "collectionId is required"
        }
    )

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        repository.observeCollection(collectionId),
        repository.observeItemsInCollection(collectionId),
    ) { collection, items ->
        CollectionDetailUiState(
            collection = collection,
            items = items,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionDetailUiState(),
    )
}
