package com.pass.hype.data.local.passwords

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Passwords::class], version = 4, exportSchema = false)
abstract class PasswordDatabase: RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
}