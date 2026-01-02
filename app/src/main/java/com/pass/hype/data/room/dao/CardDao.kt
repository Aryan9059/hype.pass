package com.pass.hype.data.room.dao

import androidx.lifecycle.LiveData
import androidx. room.Dao
import androidx.room. Query
import androidx.room. Upsert
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room. model.CardSubType
import com. pass.hype. data.room.model.CardType
import kotlinx.coroutines.flow. Flow

@Dao
interface CardDao {

    @Upsert
    suspend fun upsertCard(card: Card)

    @Query("SELECT * FROM cards WHERE cardId = :cardId")
    suspend fun getCardById(cardId: Int): Card?

    @Query("DELETE FROM cards WHERE cardId = :cardId")
    suspend fun deleteCard(cardId: Int)

    @Query("DELETE FROM cards")
    suspend fun deleteAllCards()

    @Query("SELECT * FROM cards ORDER BY isPinned DESC, isFavorite DESC, updatedAt DESC")
    fun getAllCards(): LiveData<List<Card>>

    @Query("SELECT * FROM cards ORDER BY isPinned DESC, isFavorite DESC, updatedAt DESC")
    fun getAllCardsFlow(): Flow<List<Card>>

    @Query("SELECT * FROM cards")
    suspend fun getAllCardsSync(): List<Card>

    // Filter queries
    @Query("SELECT * FROM cards WHERE cardType = :cardType ORDER BY updatedAt DESC")
    fun getCardsByType(cardType:  CardType): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE cardSubType = :subType ORDER BY updatedAt DESC")
    fun getCardsBySubType(subType:  CardSubType): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedCards(): Flow<List<Card>>

    // Search
    @Query("""
        SELECT * FROM cards 
        WHERE cardName LIKE '%' || :query || '%' 
        OR holderName LIKE '%' || :query || '%'
        OR cardNumber LIKE '%' || :query || '%'
        OR issuer LIKE '%' || :query || '%'
        OR notes LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchCards(query: String): Flow<List<Card>>

    // Expiry tracking
    @Query("""
        SELECT * FROM cards 
        WHERE expiryDate IS NOT NULL 
        AND expiryDate != ''
        ORDER BY expiryDate ASC
    """)
    fun getCardsWithExpiry(): Flow<List<Card>>

    // Update access
    @Query("""
        UPDATE cards 
        SET lastAccessedAt = :timestamp, accessCount = accessCount + 1 
        WHERE cardId = :cardId
    """)
    suspend fun updateCardAccess(cardId: Int, timestamp:  Long = System.currentTimeMillis())

    // Toggle favorite
    @Query("UPDATE cards SET isFavorite = NOT isFavorite WHERE cardId = :cardId")
    suspend fun toggleFavorite(cardId: Int)

    // Toggle pinned
    @Query("UPDATE cards SET isPinned = NOT isPinned WHERE cardId = :cardId")
    suspend fun togglePinned(cardId: Int)

    // Toggle lock
    @Query("UPDATE cards SET isLocked = NOT isLocked WHERE cardId = :cardId")
    suspend fun toggleLock(cardId: Int)

    // Check duplicates
    @Query("SELECT COUNT(*) FROM cards WHERE cardNumber = :cardNumber AND cardId != :excludeId")
    suspend fun checkDuplicateCard(cardNumber: String, excludeId:  Int = 0): Int

    // Recently accessed
    @Query("SELECT * FROM cards WHERE lastAccessedAt IS NOT NULL ORDER BY lastAccessedAt DESC LIMIT 10")
    fun getRecentlyAccessedCards(): Flow<List<Card>>
}