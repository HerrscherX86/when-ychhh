package id.my.f1zz.whenyah.ui.item

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.domain.i18n.LanguageStore
import id.my.f1zz.whenyah.domain.validation.ItemValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ItemFormState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val notes: String = "",
    val purchaseUrl: String = "",
    val collectionId: UUID? = null,
    val imageRelativePath: String? = null,
    val collections: List<CollectionEntity> = emptyList(),
    val nameError: String? = null,
    val priceError: String? = null,
    val purchaseUrlError: String? = null,
    val saveError: String? = null,
    val savedItemId: UUID? = null,
)

@HiltViewModel
class ItemEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WishlistRepository,
    private val languageStore: LanguageStore,
) : ViewModel() {
    private val itemId: UUID? = savedStateHandle.get<String>("itemId")?.let(UUID::fromString)
    private val initialCollectionId: UUID? =
        savedStateHandle.get<String>("collectionId")?.let(UUID::fromString)

    private val _uiState = MutableStateFlow(
        ItemFormState(
            isEditing = itemId != null,
            isLoading = itemId != null,
            collectionId = initialCollectionId,
        )
    )
    val uiState: StateFlow<ItemFormState> = _uiState.asStateFlow()

    private var originalImagePath: String? = null
    private var pendingImagePath: String? = null

    init {
        viewModelScope.launch {
            repository.observeCollections().collect { collections ->
                _uiState.update { state ->
                    state.copy(collections = collections.map { it.collection })
                }
            }
        }

        if (itemId != null) {
            viewModelScope.launch {
                val item = repository.getItem(itemId)
                if (item == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saveError = languageStore.strings().itemNotFound,
                        )
                    }
                    return@launch
                }

                originalImagePath = item.imageRelativePath
                _uiState.update {
                    it.copy(
                        name = item.name,
                        price = item.targetPriceIdr.toString(),
                        category = item.category.orEmpty(),
                        notes = item.notes.orEmpty(),
                        purchaseUrl = item.purchaseUrl.orEmpty(),
                        collectionId = item.collectionId,
                        imageRelativePath = item.imageRelativePath,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, saveError = null) }
    }

    fun onPriceChange(value: String) {
        val digitsOnly = value.filter(Char::isDigit).take(18)
        _uiState.update { it.copy(price = digitsOnly, priceError = null, saveError = null) }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { it.copy(category = value, saveError = null) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value, saveError = null) }
    }

    fun onPurchaseUrlChange(value: String) {
        _uiState.update { it.copy(purchaseUrl = value, purchaseUrlError = null, saveError = null) }
    }

    fun onCollectionChange(value: UUID?) {
        _uiState.update { it.copy(collectionId = value, saveError = null) }
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            runCatching { repository.copyImage(uri) }
                .onSuccess { path ->
                    pendingImagePath?.let { repository.deleteImage(it) }
                    pendingImagePath = path
                    _uiState.update {
                        it.copy(imageRelativePath = path, saveError = null)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(saveError = languageStore.strings().couldNotLoadImage)
                    }
                }
        }
    }

    fun onRemoveImage() {
        val current = _uiState.value.imageRelativePath
        if (current != null && current != originalImagePath) {
            pendingImagePath = null
            viewModelScope.launch { repository.deleteImage(current) }
        }
        _uiState.update { it.copy(imageRelativePath = null, saveError = null) }
    }

    fun save() {
        val strings = languageStore.strings()
        val state = _uiState.value
        val nameError = if (ItemValidation.isValidName(state.name)) {
            null
        } else {
            strings.enterItemName
        }
        val price = ItemValidation.parseIdrAmount(state.price)
        val priceError = if (price == null) {
            strings.enterValidIdr
        } else {
            null
        }
        val purchaseUrlError = if (ItemValidation.isValidPurchaseUrl(state.purchaseUrl)) {
            null
        } else {
            strings.enterValidUrl
        }

        if (nameError != null || priceError != null || purchaseUrlError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    priceError = priceError,
                    purchaseUrlError = purchaseUrlError,
                    saveError = null,
                )
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            runCatching {
                repository.saveItem(
                    itemId = itemId,
                    name = state.name,
                    targetPriceIdr = checkNotNull(price),
                    imageRelativePath = state.imageRelativePath,
                    purchaseUrl = state.purchaseUrl.ifBlank { null },
                    notes = state.notes.ifBlank { null },
                    category = state.category.ifBlank { null },
                    collectionId = state.collectionId,
                )
            }.onSuccess { savedId ->
                pendingImagePath = null
                _uiState.update {
                    it.copy(isSaving = false, savedItemId = savedId)
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = languageStore.strings().couldNotSaveItem,
                    )
                }
            }
        }
    }
}
