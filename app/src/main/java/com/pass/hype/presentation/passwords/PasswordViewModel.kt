package com.pass.hype.presentation.passwords

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.hype.HypePass
import com.pass.hype.data.local.passwords.Passwords
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class PasswordViewModel: ViewModel() {

    val passwordDao = HypePass.passwordDatabase.passwordDao()

    val passwordsList: LiveData<List<Passwords>> = passwordDao.getAllPasswords()

    fun addPassword(passwords: Passwords) {
        viewModelScope.launch(Dispatchers.IO) {
            passwordDao.upsertPassword(passwords)
        }
    }

    fun deletePassword(passwordId: Int){
        viewModelScope.launch(Dispatchers.IO) {
            passwordDao.deletePassword(passwordId)
        }
    }
}