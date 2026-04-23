package com.pass.hype.presentation.passwords

import androidx.lifecycle.LiveData
import androidx. lifecycle. ViewModel
import androidx. lifecycle.viewModelScope
import com.pass.hype.HypePass
import com.pass.hype.data.room.model. Passwords
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines. launch
import kotlinx.coroutines.withContext

class PasswordViewModel :  ViewModel() {
    private val passwordDao = HypePass. passwordDatabase.passwordDao()

    val passwordsList: LiveData<List<Passwords>> = passwordDao.getAllPasswords()

    fun addPassword(passwords: Passwords) {
        viewModelScope.launch(Dispatchers.IO) {
            passwordDao.upsertPassword(passwords)
        }
    }

    fun updatePassword(passwords: Passwords) {
        viewModelScope.launch(Dispatchers.IO) {
            passwordDao. upsertPassword(
                passwords.copy(
                    editTime = System.currentTimeMillis().toString(),
                    edited = true
                )
            )
        }
    }

    fun deletePassword(passwordId:  Int) {
        viewModelScope.launch(Dispatchers. IO) {
            passwordDao.deletePassword(passwordId)
        }
    }

    suspend fun getPasswordById(passwordId: Int): Passwords? {
        return withContext(Dispatchers. IO) {
            passwordDao.getPasswordById(passwordId)
        }
    }
}