package com.pass.hype.presentation.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.hype.HypePass
import com.pass.hype.data.local.cards.Cards
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CardViewModel: ViewModel() {
    val cardDao = HypePass.cardDatabase.cardDao()

    val cardList: LiveData<List<Cards>> = cardDao.getAllCards()

    fun addCard(cards: Cards) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.upsertCard(cards)
        }
    }

    fun deleteCard(cardId: Int){
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.deleteCard(cardId)
        }
    }
}