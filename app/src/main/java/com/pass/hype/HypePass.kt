package com.pass.hype

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db. SupportSQLiteDatabase
import com.pass. hype.data. room.database.CardDatabase
import com. pass.hype. data.room.database. PasswordDatabase

class HypePass :  Application() {

    companion object {
        lateinit var passwordDatabase: PasswordDatabase
            private set
        lateinit var cardDatabase: CardDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        passwordDatabase = Room.databaseBuilder(
            applicationContext,
            PasswordDatabase::class.java,
            "Passwords"
        )
            .fallbackToDestructiveMigration(true)
            .build()

        cardDatabase = Room. databaseBuilder(
            applicationContext,
            CardDatabase::class. java,
            "Cards"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
}