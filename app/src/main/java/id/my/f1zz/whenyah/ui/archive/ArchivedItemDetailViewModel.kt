package id.my.f1zz.whenyah.ui.archive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.PurchaseRecordEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.repository.PurchaseRepository
import id.my.f1zz.whenyah.data.repository.SavingsRepository
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ArchivedItemDetailUiState(
    val item: WishlistItemEntity? = null,
    val purchaseRecord: PurchaseRecordEntity? = null,
    val events: List<SavingsEventEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ArchivedItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wishlistRepository: WishlistRepository,
    private val purchaseRepository: PurchaseRepository,
    savingsRepository: SavingsRepository,
) : ViewModel() {
    private val itemId = UUID.fromString(
        checkNotNull(savedStateHandle.get<String>("itemId")) {
            "itemId is required"
        }
    )

    val uiState: StateFlow<ArchivedItemDetailUiState> = combine(
        wishlistRepository.observeItem(itemId),
        purchaseRepository.observeLatestPurchaseRecord(itemId),
        savingsRepository.observeEvents(itemId),
    ) { item, purchaseRecord, events ->
        ArchivedItemDetailUiState(
            item = item,
            purchaseRecord = purchaseRecord,
            events = events,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArchivedItemDetailUiState(),
    )

    private val _restored = MutableStateFlow(false)
    val restored: StateFlow<Boolean> = _restored.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun restoreItem() {
        viewModelScope.launch {
            purchaseRepository.restoreItem(itemId).fold(
                onSuccess = { _restored.value = true },
                onFailure = { failure ->
                    _error.value = failure.message ?: "Could not restore the item"
                },
            )
        }
    }

    fun deleteItem() {
        viewModelScope.launch {
            runCatching { wishlistRepository.deleteItem(itemId) }.fold(
                onSuccess = { _deleted.value = true },
                onFailure = { failure ->
                    _error.value = failure.message ?: "Could not delete the item"
                },
            )
        }
    }
}
