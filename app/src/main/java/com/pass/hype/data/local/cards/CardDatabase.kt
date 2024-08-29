package com.pass.hype.data.local.cards

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Cards::class], version = 1, exportSchema = false)
abstract class CardDatabase: RoomDatabase() {

    abstract fun cardDao(): CardDao
}