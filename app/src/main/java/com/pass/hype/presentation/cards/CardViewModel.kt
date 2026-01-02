package com.pass.hype.presentation.cards

import androidx.lifecycle.LiveData
import androidx. lifecycle. ViewModel
import androidx. lifecycle.viewModelScope
import com.pass.hype.HypePass
import com.pass.hype.data.room.model. Card
import com. pass.hype. data.room.model.CardSubType
import com. pass.hype. data.room.model.CardType
import com.pass.hype.utils.CardUtils
import kotlinx. coroutines. Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow. MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines. flow.asStateFlow
import kotlinx. coroutines.launch

data class CardUiState(
    val selectedCardType: CardType?  = null,
    val selectedSubType:  CardSubType? = null,
    val searchQuery: String = "",
    val showOnlyFavorites: Boolean = false,
    val showOnlyExpiring: Boolean = false
)

class CardViewModel :  ViewModel() {
    private val cardDao = HypePass.cardDatabase.cardDao()

    private val _uiState = MutableStateFlow(CardUiState())
    val uiState:  StateFlow<CardUiState> = _uiState.asStateFlow()

    val cardList: LiveData<List<Card>> = cardDao. getAllCards()

    fun searchCards(query: String): Flow<List<Card>> = cardDao.searchCards(query)

    fun getCardsByType(cardType:  CardType): Flow<List<Card>> = cardDao.getCardsByType(cardType)

    fun getCardsBySubType(subType: CardSubType): Flow<List<Card>> = cardDao.getCardsBySubType(subType)

    fun getFavoriteCards(): Flow<List<Card>> = cardDao.getFavoriteCards()

    fun getRecentlyAccessedCards(): Flow<List<Card>> = cardDao.getRecentlyAccessedCards()

    fun getCardsWithExpiry(): Flow<List<Card>> = cardDao.getCardsWithExpiry()

    fun addCard(card: Card) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.upsertCard(card)
        }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.upsertCard(card. copy(updatedAt = System. currentTimeMillis()))
        }
    }

    fun deleteCard(cardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao. deleteCard(cardId)
        }
    }

    fun toggleFavorite(cardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.toggleFavorite(cardId)
        }
    }

    fun togglePinned(cardId: Int) {
        viewModelScope. launch(Dispatchers.IO) {
            cardDao.togglePinned(cardId)
        }
    }

    fun toggleLock(cardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.toggleLock(cardId)
        }
    }

    fun recordCardAccess(cardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao. updateCardAccess(cardId)
        }
    }

    suspend fun checkDuplicate(cardNumber:  String, excludeId: Int = 0): Boolean {
        return cardDao. checkDuplicateCard(cardNumber, excludeId) > 0
    }

    fun updateFilter(
        cardType: CardType?  = _uiState.value.selectedCardType,
        subType:  CardSubType? = _uiState. value.selectedSubType,
        searchQuery: String = _uiState. value.searchQuery,
        showOnlyFavorites: Boolean = _uiState.value.showOnlyFavorites,
        showOnlyExpiring: Boolean = _uiState. value.showOnlyExpiring
    ) {
        _uiState.value = CardUiState(
            selectedCardType = cardType,
            selectedSubType = subType,
            searchQuery = searchQuery,
            showOnlyFavorites = showOnlyFavorites,
            showOnlyExpiring = showOnlyExpiring
        )
    }

    fun clearFilters() {
        _uiState. value = CardUiState()
    }
}