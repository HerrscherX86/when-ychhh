package id.my.f1zz.whenyah.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.f1zz.whenyah.data.backup.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isExporting: Boolean = false,
    val isRestoring: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun export(uri: Uri) {
        if (_uiState.value.isExporting || _uiState.value.isRestoring) return

        _uiState.update {
            it.copy(isExporting = true, message = null, error = null)
        }
        viewModelScope.launch {
            backupManager.exportTo(uri).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            message = "Exported ${result.itemCount} items and " +
                                "${result.imageCount} images.",
                        )
                    }
                },
                onFailure = { failure ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            error = failure.message ?: "Could not export the backup.",
                        )
                    }
                },
            )
        }
    }

    fun restore(uri: Uri) {
        if (_uiState.value.isExporting || _uiState.value.isRestoring) return

        _uiState.update {
            it.copy(isRestoring = true, message = null, error = null)
        }
        viewModelScope.launch {
            backupManager.restoreFrom(uri).fold(
                onSuccess = { result ->
                    val settingsSuffix = if (result.settingsApplied) {
                        ""
                    } else {
                        " Settings could not be restored."
                    }
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            message = "Restored ${result.itemCount} items and " +
                                "${result.imageCount} images.$settingsSuffix",
                        )
                    }
                },
                onFailure = { failure ->
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            error = failure.message ?: "Could not restore the backup.",
                        )
                    }
                },
            )
        }
    }
}
