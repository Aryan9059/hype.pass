package com.pass.hype.data.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pass.hype.data.room.model.Cards
import com.pass.hype.data.room.dao.CardDao

@Database(entities = [Cards::class], version = 1, exportSchema = false)
abstract class CardDatabase: RoomDatabase() {

    abstract fun cardDao(): CardDao
}