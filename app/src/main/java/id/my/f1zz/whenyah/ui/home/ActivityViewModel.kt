package id.my.f1zz.whenyah.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.repository.SavingsRepository
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ActivityUiState(
    val activities: List<DashboardActivity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    wishlistRepository: WishlistRepository,
    savingsRepository: SavingsRepository,
) : ViewModel() {
    val uiState: StateFlow<ActivityUiState> = combine(
        wishlistRepository.observeActiveItemsWithSavings(),
        savingsRepository.observeAllEvents(),
    ) { items, events ->
        val itemsById = items.associateBy { it.item.id }
        val activities = events
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
            .toList()

        ActivityUiState(
            activities = activities,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivityUiState(),
    )
}
