package id.my.f1zz.whenyah.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.data.notification.ReminderCoordinator
import id.my.f1zz.whenyah.data.repository.PurchaseRepository
import id.my.f1zz.whenyah.data.repository.SavingsPlanRepository
import id.my.f1zz.whenyah.data.repository.SavingsRepository
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.domain.i18n.LanguageStore
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.plan.SavingsPlanCalculator
import id.my.f1zz.whenyah.domain.plan.SavingsPlanProjection
import id.my.f1zz.whenyah.domain.plan.observeToday
import id.my.f1zz.whenyah.domain.savings.SavingsCalculator
import id.my.f1zz.whenyah.domain.savings.SavingsSummary
import id.my.f1zz.whenyah.domain.savings.TransactionType
import id.my.f1zz.whenyah.domain.validation.ItemValidation
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

data class ItemDetailUiState(
    val item: WishlistItemEntity? = null,
    val collectionName: String? = null,
    val savings: SavingsSummary = SavingsSummary.Empty,
    val events: List<SavingsEventEntity> = emptyList(),
    val plan: SavingsPlanEntity? = null,
    val planProjection: SavingsPlanProjection? = null,
    val isLoading: Boolean = true,
)

data class TransactionFormState(
    val type: TransactionType? = null,
    val amount: String = "",
    val note: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

data class PurchaseUiState(
    val isPurchasing: Boolean = false,
    val error: String? = null,
    val purchased: Boolean = false,
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WishlistRepository,
    private val savingsRepository: SavingsRepository,
    private val savingsPlanRepository: SavingsPlanRepository,
    private val purchaseRepository: PurchaseRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val languageStore: LanguageStore,
) : ViewModel() {
    private val itemId = UUID.fromString(
        checkNotNull(savedStateHandle.get<String>("itemId")) {
            "itemId is required"
        }
    )

    val uiState: StateFlow<ItemDetailUiState> = combine(
        repository.observeItem(itemId),
        repository.observeCollections(),
        savingsRepository.observeEvents(itemId),
        savingsPlanRepository.observePlan(itemId),
        observeToday(),
    ) { item, collections, events, plan, today ->
        val collectionName = item?.collectionId?.let { collectionId ->
            collections.firstOrNull { it.collection.id == collectionId }?.collection?.name
        }
        val savings = item?.let {
            SavingsCalculator.summarize(events, it.targetPriceIdr)
        } ?: SavingsSummary.Empty
        val planProjection = if (item != null && plan != null) {
            runCatching {
                SavingsPlanCalculator.project(
                    targetPriceIdr = item.targetPriceIdr,
                    availableBalanceIdr = savings.availableIdr,
                    contributionIdr = plan.contributionIdr,
                    frequency = plan.frequency,
                    startDate = today,
                )
            }.getOrNull()
        } else {
            null
        }

        ItemDetailUiState(
            item = item,
            collectionName = collectionName,
            savings = savings,
            events = events,
            plan = plan,
            planProjection = planProjection,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItemDetailUiState(),
    )

    private val _transactionForm = MutableStateFlow(TransactionFormState())
    val transactionForm: StateFlow<TransactionFormState> = _transactionForm.asStateFlow()

    private val _purchaseState = MutableStateFlow(PurchaseUiState())
    val purchaseState: StateFlow<PurchaseUiState> = _purchaseState.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _transactionCompleted = MutableStateFlow<TransactionType?>(null)
    val transactionCompleted: StateFlow<TransactionType?> = _transactionCompleted.asStateFlow()

    fun consumeTransactionCompleted() {
        _transactionCompleted.value = null
    }

    fun startTransaction(type: TransactionType) {
        _transactionForm.value = TransactionFormState(type = type)
    }

    fun dismissTransaction() {
        if (!_transactionForm.value.isSaving) {
            _transactionForm.value = TransactionFormState()
        }
    }

    fun onTransactionAmountChange(value: String) {
        val digitsOnly = value.filter(Char::isDigit).take(18)
        _transactionForm.update { it.copy(amount = digitsOnly, error = null) }
    }

    fun onTransactionNoteChange(value: String) {
        _transactionForm.update { it.copy(note = value, error = null) }
    }

    fun submitTransaction() {
        val form = _transactionForm.value
        val type = form.type ?: return
        val amount = ItemValidation.parseIdrAmount(form.amount)

        val strings = languageStore.strings()
        if (amount == null || amount <= 0) {
            _transactionForm.update {
                it.copy(error = strings.enterValidAmount)
            }
            return
        }

        if (type == TransactionType.WITHDRAWAL &&
            amount > uiState.value.savings.availableIdr
        ) {
            _transactionForm.update {
                it.copy(error = strings.withdrawalExceeds)
            }
            return
        }

        _transactionForm.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = when (type) {
                TransactionType.DEPOSIT -> savingsRepository.recordDeposit(
                    itemId = itemId,
                    amountIdr = amount,
                    note = form.note,
                )

                TransactionType.WITHDRAWAL -> savingsRepository.recordWithdrawal(
                    itemId = itemId,
                    amountIdr = amount,
                    note = form.note,
                )
            }

            result.fold(
                onSuccess = {
                    _transactionCompleted.value = type
                    _transactionForm.value = TransactionFormState()
                },
                onFailure = {
                    _transactionForm.update {
                        it.copy(
                            isSaving = false,
                            error = languageStore.strings().couldNotRecordTransaction,
                        )
                    }
                },
            )
        }
    }

    fun purchaseItem() {
        val item = uiState.value.item ?: return
        if (item.status != ItemStatus.ACTIVE || _purchaseState.value.isPurchasing) return

        _purchaseState.update { it.copy(isPurchasing = true, error = null) }

        viewModelScope.launch {
            val result = purchaseRepository.purchaseItem(itemId)
            if (result.isSuccess) {
                runCatching {
                    reminderCoordinator.syncPlanReminderForItem(itemId)
                }
                _purchaseState.update {
                    it.copy(isPurchasing = false, purchased = true)
                }
            } else {
                _purchaseState.update {
                    it.copy(
                        isPurchasing = false,
                        error = languageStore.strings().couldNotMarkPurchased,
                    )
                }
            }
        }
    }

    fun clearPurchaseError() {
        _purchaseState.update { it.copy(error = null) }
    }

    fun deleteItem() {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            runCatching {
                reminderCoordinator.syncPlanReminderForItem(itemId)
            }
            _deleted.value = true
        }
    }
}
