package id.my.f1zz.whenyah.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.notification.ReminderCoordinator
import id.my.f1zz.whenyah.data.repository.SavingsPlanRepository
import id.my.f1zz.whenyah.data.repository.SavingsRepository
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.plan.SavingsPlanCalculator
import id.my.f1zz.whenyah.domain.plan.SavingsPlanProjection
import id.my.f1zz.whenyah.domain.plan.observeToday
import id.my.f1zz.whenyah.domain.validation.ItemValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

data class SavingsPlanUiState(
    val item: WishlistItemEntity? = null,
    val availableIdr: Long = 0,
    val plan: SavingsPlanEntity? = null,
    val contribution: String = "",
    val frequency: PlanFrequency = PlanFrequency.WEEKLY,
    val reminderEnabled: Boolean = false,
    val reminderLocalTime: LocalTime = LocalTime.of(9, 0),
    val projection: SavingsPlanProjection? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
)

@HiltViewModel
class SavingsPlanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wishlistRepository: WishlistRepository,
    private val savingsRepository: SavingsRepository,
    private val savingsPlanRepository: SavingsPlanRepository,
    private val reminderCoordinator: ReminderCoordinator,
) : ViewModel() {
    private val itemId = UUID.fromString(
        checkNotNull(savedStateHandle.get<String>("itemId")) {
            "itemId is required"
        }
    )

    private val _uiState = MutableStateFlow(SavingsPlanUiState())
    val uiState: StateFlow<SavingsPlanUiState> = _uiState.asStateFlow()

    private var today: LocalDate = LocalDate.now()

    init {
        viewModelScope.launch {
            val item = wishlistRepository.getItem(itemId)
            val available = savingsRepository.getBalance(itemId)
            val plan = savingsPlanRepository.getPlan(itemId)
            val state = SavingsPlanUiState(
                item = item,
                availableIdr = available,
                plan = plan,
                contribution = plan?.contributionIdr?.toString().orEmpty(),
                frequency = plan?.frequency ?: PlanFrequency.WEEKLY,
                reminderEnabled = plan?.reminderEnabled ?: false,
                reminderLocalTime = plan?.reminderLocalTime ?: LocalTime.of(9, 0),
                isLoading = false,
            )
            _uiState.value = withProjection(state, today)
        }

        viewModelScope.launch {
            observeToday().collect { date ->
                today = date
                _uiState.update { state -> withProjection(state, date) }
            }
        }
    }

    fun onContributionChange(value: String) {
        val digitsOnly = value.filter(Char::isDigit).take(18)
        _uiState.update { state ->
            withProjection(
                state.copy(contribution = digitsOnly, error = null),
                today,
            )
        }
    }

    fun onFrequencyChange(frequency: PlanFrequency) {
        _uiState.update { state ->
            withProjection(
                state.copy(frequency = frequency, error = null),
                today,
            )
        }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update {
            it.copy(reminderEnabled = enabled, error = null)
        }
    }

    fun onReminderTimeChange(time: LocalTime) {
        _uiState.update {
            it.copy(reminderLocalTime = time, error = null)
        }
    }

    fun savePlan() {
        val state = _uiState.value
        if (state.item == null) {
            _uiState.update { it.copy(error = "Item not found") }
            return
        }

        val amount = ItemValidation.parseIdrAmount(state.contribution)
        if (amount == null || amount <= 0) {
            _uiState.update {
                it.copy(error = "Enter a valid contribution greater than zero")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            runCatching {
                savingsPlanRepository.savePlan(
                    itemId = itemId,
                    contributionIdr = amount,
                    frequency = state.frequency,
                    reminderEnabled = state.reminderEnabled,
                    reminderLocalTime = state.reminderLocalTime,
                )
            }.onSuccess {
                runCatching {
                    reminderCoordinator.syncPlanReminderForItem(itemId, force = true)
                }
                _uiState.update { current ->
                    current.copy(isSaving = false, saved = true)
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = failure.message ?: "Could not save the plan",
                    )
                }
            }
        }
    }

    fun togglePlanActive() {
        val plan = _uiState.value.plan ?: return

        viewModelScope.launch {
            runCatching {
                savingsPlanRepository.setActive(itemId, active = !plan.active)
            }.onSuccess {
                runCatching {
                    reminderCoordinator.syncPlanReminderForItem(itemId, force = true)
                }
                _uiState.update { state ->
                    state.copy(plan = plan.copy(active = !plan.active), error = null)
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(error = failure.message ?: "Could not update the plan")
                }
            }
        }
    }

    fun deletePlan() {
        viewModelScope.launch {
            runCatching {
                savingsPlanRepository.deletePlan(itemId)
            }.onSuccess {
                runCatching {
                    reminderCoordinator.syncPlanReminderForItem(itemId, force = true)
                }
                _uiState.update {
                    it.copy(plan = null, deleted = true)
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(error = failure.message ?: "Could not delete the plan")
                }
            }
        }
    }

    private fun withProjection(
        state: SavingsPlanUiState,
        date: LocalDate,
    ): SavingsPlanUiState {
        val item = state.item ?: return state.copy(projection = null)
        val amount = ItemValidation.parseIdrAmount(state.contribution)
        val projection = if (amount != null && amount > 0) {
            runCatching {
                SavingsPlanCalculator.project(
                    targetPriceIdr = item.targetPriceIdr,
                    availableBalanceIdr = state.availableIdr,
                    contributionIdr = amount,
                    frequency = state.frequency,
                    startDate = date,
                )
            }.getOrNull()
        } else {
            null
        }

        return state.copy(projection = projection)
    }
}
