package com.pass.hype.data.local.cards

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CardDao {

    @Upsert
    fun upsertCard(cards: Cards)

    @Query("SELECT * FROM Cards WHERE cardId = :cardId")
    fun getCardById(cardId: Int): Cards?

    @Query("DELETE FROM Cards WHERE cardId = :cardId")
    fun deleteCard(cardId: Int)

    @Query("SELECT * FROM Cards")
    fun getAllCards(): LiveData<List<Cards>>
}