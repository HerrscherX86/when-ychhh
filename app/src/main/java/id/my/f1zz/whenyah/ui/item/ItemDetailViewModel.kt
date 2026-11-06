package id.my.f1zz.whenyah.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ItemDetailUiState(
    val item: WishlistItemEntity? = null,
    val collectionName: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WishlistRepository,
) : ViewModel() {
    private val itemId = UUID.fromString(
        checkNotNull(savedStateHandle.get<String>("itemId")) {
            "itemId is required"
        }
    )

    val uiState: StateFlow<ItemDetailUiState> = combine(
        repository.observeItem(itemId),
        repository.observeCollections(),
    ) { item, collections ->
        val collectionName = item?.collectionId?.let { collectionId ->
            collections.firstOrNull { it.collection.id == collectionId }?.collection?.name
        }
        ItemDetailUiState(
            item = item,
            collectionName = collectionName,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItemDetailUiState(),
    )

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun deleteItem() {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            _deleted.value = true
        }
    }
}
