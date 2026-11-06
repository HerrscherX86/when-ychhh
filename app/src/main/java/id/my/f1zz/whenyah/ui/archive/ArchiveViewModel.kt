package id.my.f1zz.whenyah.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.PurchaseRecordEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.repository.PurchaseRepository
import id.my.f1zz.whenyah.domain.savings.SavingsSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ArchiveEntry(
    val item: WishlistItemEntity,
    val savings: SavingsSummary,
    val purchaseRecord: PurchaseRecordEntity?,
)

data class ArchiveUiState(
    val items: List<ArchiveEntry> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    purchaseRepository: PurchaseRepository,
) : ViewModel() {
    val uiState: StateFlow<ArchiveUiState> = combine(
        purchaseRepository.observePurchasedItemsWithSavings(),
        purchaseRepository.observePurchaseRecords(),
    ) { items, records ->
        val latestByItem = records.groupBy { it.itemId }
            .mapValues { (_, itemRecords) -> itemRecords.first() }

        ArchiveUiState(
            items = items.map { entry ->
                ArchiveEntry(
                    item = entry.item,
                    savings = entry.savings,
                    purchaseRecord = latestByItem[entry.item.id],
                )
            },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArchiveUiState(),
    )
}
