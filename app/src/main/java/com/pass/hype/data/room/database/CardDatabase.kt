package com.pass.hype.data.room.database

import androidx.room.Database
import androidx.room. RoomDatabase
import androidx.room.TypeConverters
import com.pass.hype.data.room. converters.CardConverters
import com.pass. hype.data. room.dao.CardDao
import com.pass.hype.data.room.model.Card

@Database(
    entities = [Card:: class],
    version = 2,
    exportSchema = false
)
@TypeConverters(CardConverters::class)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
}