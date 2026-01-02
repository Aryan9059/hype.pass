package com.pass.hype.autofill.repository

import android.content.Context
import com.pass. hype.HypePass
import com.pass.hype.data.room.model.Passwords
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.withContext

class AutofillRepository(private val context: Context) {

    private val passwordDao = HypePass.passwordDatabase.passwordDao()

    suspend fun getAllPasswords(): List<Passwords> = withContext(Dispatchers.IO) {
        passwordDao.getAllPasswordsSync()
    }

    suspend fun getPasswordsByAppName(appName:  String): List<Passwords> = withContext(Dispatchers. IO) {
        passwordDao.getPasswordsByAppName(appName)
    }

    suspend fun searchPasswords(query: String): List<Passwords> = withContext(Dispatchers.IO) {
        val allPasswords = passwordDao.getAllPasswordsSync()
        allPasswords.filter { password ->
            password.appName.contains(query, ignoreCase = true) ||
                    password.email.contains(query, ignoreCase = true)
        }
    }

    suspend fun savePassword(password:  Passwords) = withContext(Dispatchers.IO) {
        passwordDao.upsertPassword(password)
    }
}