package com.pass.hype.data.local.passwords

import androidx.compose.ui.platform.LocalContext
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pass.hype.HypePass
import com.pass.hype.HypePass.Companion
import com.pass.hype.data.local.cards.CardDatabase

@Database(entities = [Passwords::class], version = 3, exportSchema = false)
abstract class PasswordDatabase: RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
}