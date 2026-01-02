package com.pass.hype.presentation.settings.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle. ViewModel
import androidx. lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com. pass.hype. HypePass
import com.pass.hype.data.backup. BackupRepository
import com.pass.hype.data.backup.BackupResult
import com.pass. hype.data. backup.RestoreResult
import kotlinx. coroutines.flow.MutableStateFlow
import kotlinx. coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String?  = null,
    val isError: Boolean = false
)

class BackupViewModel(
    private val backupRepository:  BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun createBackup(pin: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState(isLoading = true)

            when (val result = backupRepository.createBackup(pin)) {
                is BackupResult.Success -> {
                    _uiState.value = BackupUiState(
                        message = result.message,
                        isError = false
                    )
                }
                is BackupResult.Error -> {
                    _uiState. value = BackupUiState(
                        message = result. message,
                        isError = true
                    )
                }
            }
        }
    }

    fun restoreBackup(uri: Uri, pin: String, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = BackupUiState(isLoading = true)

            when (val result = backupRepository. restoreBackup(uri, pin, replaceExisting)) {
                is RestoreResult.Success -> {
                    _uiState.value = BackupUiState(
                        message = "Restored ${result.passwordCount} passwords and ${result.cardCount} cards",
                        isError = false
                    )
                }
                is RestoreResult.Error -> {
                    _uiState. value = BackupUiState(
                        message = result.message,
                        isError = true
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider. Factory {
            return object : ViewModelProvider. Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass:  Class<T>): T {
                    val repository = BackupRepository(
                        passwordDao = HypePass.passwordDatabase.passwordDao(),
                        cardDao = HypePass.cardDatabase. cardDao(),
                        context = context. applicationContext
                    )
                    return BackupViewModel(repository) as T
                }
            }
        }
    }
}