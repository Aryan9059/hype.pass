package com.pass.hype.autofill.repository

import android. content.Context
import com.pass.hype.HypePass
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room. model.CardSubType
import com. pass.hype. data.room.model.CardType
import com.pass.hype.data.room. model.Passwords
import com.pass. hype.utils. CardUtils
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.withContext

class AutofillRepository(private val context: Context) {

    private val passwordDao = HypePass.passwordDatabase.passwordDao()
    private val cardDao = HypePass.cardDatabase.cardDao()

    suspend fun getAllPasswords(): List<Passwords> = withContext(Dispatchers. IO) {
        passwordDao.getAllPasswordsSync()
    }

    suspend fun getPasswordsByAppName(appName: String): List<Passwords> = withContext(Dispatchers.IO) {
        passwordDao.getPasswordsByAppName(appName)
    }

    suspend fun searchPasswords(query: String): List<Passwords> = withContext(Dispatchers.IO) {
        val allPasswords = passwordDao.getAllPasswordsSync()
        allPasswords.filter { password ->
            password.appName.contains(query, ignoreCase = true) ||
                    password.email.contains(query, ignoreCase = true)
        }
    }

    suspend fun savePassword(password:  Passwords) = withContext(Dispatchers. IO) {
        passwordDao.upsertPassword(password)
    }

    // Card methods for potential future card autofill
    suspend fun getAllCards(): List<Card> = withContext(Dispatchers.IO) {
        cardDao.getAllCardsSync()
    }

    suspend fun getFinancialCards(): List<Card> = withContext(Dispatchers. IO) {
        cardDao.getAllCardsSync().filter { it.cardType == CardType. FINANCIAL }
    }

    suspend fun saveCard(card: Card) = withContext(Dispatchers.IO) {
        cardDao.upsertCard(card)
    }
}