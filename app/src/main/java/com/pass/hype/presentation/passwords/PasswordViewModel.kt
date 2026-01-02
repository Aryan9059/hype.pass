package com.pass.hype.presentation.passwords

import androidx.lifecycle.LiveData
import androidx. lifecycle. ViewModel
import androidx. lifecycle.viewModelScope
import com.pass.hype.HypePass
import com.pass.hype.data.room.model.Passwords
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.launch

class PasswordViewModel :  ViewModel() {

    private val passwordDao = HypePass.passwordDatabase.passwordDao()

    val passwordsList: LiveData<List<Passwords>> = passwordDao.getAllPasswords()

    fun addPassword(password:  Passwords) {
        viewModelScope.launch(Dispatchers. IO) {
            passwordDao.upsertPassword(password)
        }
    }

    fun updatePassword(password:  Passwords) {
        viewModelScope. launch(Dispatchers.IO) {
            passwordDao.upsertPassword(password)
        }
    }

    fun deletePassword(passwordId: Int) {
        viewModelScope. launch(Dispatchers.IO) {
            passwordDao.deletePassword(passwordId)
        }
    }
}