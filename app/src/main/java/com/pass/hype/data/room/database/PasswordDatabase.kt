package com.pass.hype.data.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pass.hype.data.room.dao.PasswordDao
import com.pass.hype.data.room.model.Passwords

@Database(entities = [Passwords::class], version = 4, exportSchema = false)
abstract class PasswordDatabase: RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
}