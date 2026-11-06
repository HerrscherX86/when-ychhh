package id.my.f1zz.whenyah.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.repository.SavingsRepository
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.domain.savings.SavingsCalculator
import id.my.f1zz.whenyah.domain.savings.SavingsSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

data class TransactionHistoryUiState(
    val item: WishlistItemEntity? = null,
    val events: List<SavingsEventEntity> = emptyList(),
    val savings: SavingsSummary = SavingsSummary.Empty,
    val isLoading: Boolean = true,
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    wishlistRepository: WishlistRepository,
    savingsRepository: SavingsRepository,
) : ViewModel() {
    private val itemId = UUID.fromString(
        checkNotNull(savedStateHandle.get<String>("itemId")) {
            "itemId is required"
        }
    )

    val uiState: StateFlow<TransactionHistoryUiState> = combine(
        wishlistRepository.observeItem(itemId),
        savingsRepository.observeEvents(itemId),
    ) { item, events ->
        TransactionHistoryUiState(
            item = item,
            events = events,
            savings = item?.let {
                SavingsCalculator.summarize(events, it.targetPriceIdr)
            } ?: SavingsSummary.Empty,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionHistoryUiState(),
    )
}
