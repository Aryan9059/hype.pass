package com.pass.hype

import android.app.Application
import androidx.room.Room
import com.pass.hype.data.local.cards.CardDatabase
import com.pass.hype.data.local.passwords.PasswordDatabase

class HypePass: Application(){

    companion object {
        lateinit var passwordDatabase: PasswordDatabase
        lateinit var cardDatabase: CardDatabase
    }

    override fun onCreate() {
        super.onCreate()
        passwordDatabase = Room.databaseBuilder(
            applicationContext,
            PasswordDatabase::class.java,
            "Passwords"
        )
            .fallbackToDestructiveMigration()
            .build()

        cardDatabase = Room.databaseBuilder(
            applicationContext,
            CardDatabase::class.java,
            "Cards"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}