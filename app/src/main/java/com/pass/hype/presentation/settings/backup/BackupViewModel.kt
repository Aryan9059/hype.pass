package com.pass.hype.presentation.settings.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pass.hype.HypePass
import com.pass.hype.data.backup.BackupRepository
import com.pass.hype.data.backup.BackupResult
import com. pass.hype. data.backup.RestoreResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx. coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isLoading:  Boolean = false,
    val message: String?  = null,
    val isError: Boolean = false,
    val isSuccess: Boolean = false
)

class BackupViewModel(
    private val backupRepository:  BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun createBackup(pin:  String) {
        if (pin.isBlank()) {
            _uiState. value = BackupUiState(
                message = "PIN is required for backup",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = BackupUiState(isLoading = true)

            when (val result = backupRepository.createBackup(pin)) {
                is BackupResult.Success -> {
                    _uiState.value = BackupUiState(
                        message = result.message,
                        isError = false,
                        isSuccess = true
                    )
                }
                is BackupResult.Error -> {
                    _uiState. value = BackupUiState(
                        message = result. message,
                        isError = true,
                        isSuccess = false
                    )
                }
            }
        }
    }

    fun restoreBackup(uri: Uri, pin:  String, replaceExisting: Boolean = false) {
        if (pin.isBlank()) {
            _uiState.value = BackupUiState(
                message = "PIN is required for restore",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = BackupUiState(isLoading = true)

            when (val result = backupRepository.restoreBackup(uri, pin, replaceExisting)) {
                is RestoreResult.Success -> {
                    _uiState. value = BackupUiState(
                        message = "Restored ${result.passwordCount} passwords and ${result.cardCount} cards",
                        isError = false,
                        isSuccess = true
                    )
                }
                is RestoreResult.Error -> {
                    _uiState.value = BackupUiState(
                        message = result.message,
                        isError = true,
                        isSuccess = false
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            isError = false,
            isSuccess = false
        )
    }

    fun resetState() {
        _uiState.value = BackupUiState()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider. Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass:  Class<T>): T {
                    val repository = BackupRepository(
                        passwordDao = HypePass.passwordDatabase.passwordDao(),
                        cardDao = HypePass. cardDatabase.cardDao(),
                        context = context. applicationContext
                    )
                    return BackupViewModel(repository) as T
                }
            }
        }
    }
}