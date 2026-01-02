package com.pass.hype.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pass.hype.data.room.model.Cards

@Dao
interface CardDao {

    @Upsert
    suspend fun upsertCard(cards: Cards)

    @Query("SELECT * FROM Cards WHERE cardId = :cardId")
    suspend fun getCardById(cardId: Int): Cards?

    @Query("DELETE FROM Cards WHERE cardId = :cardId")
    suspend fun deleteCard(cardId: Int)

    @Query("DELETE FROM Cards")
    suspend fun deleteAllCards()

    @Query("SELECT * FROM Cards")
    fun getAllCards(): LiveData<List<Cards>>

    @Query("SELECT * FROM Cards")
    suspend fun getAllCardsSync(): List<Cards>
}