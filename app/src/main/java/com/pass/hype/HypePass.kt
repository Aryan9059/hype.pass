package com.pass.hype

import android.app.Application
import androidx.room.Room
import com.pass.hype.data.room.database.CardDatabase
import com.pass.hype.data.room.database.PasswordDatabase

class HypePass :  Application() {

    companion object {
        lateinit var passwordDatabase: PasswordDatabase
            private set
        lateinit var cardDatabase: CardDatabase
            private set

        private var isInitialized = false

        fun isDatabaseInitialized(): Boolean = isInitialized
    }

    override fun onCreate() {
        super.onCreate()

        passwordDatabase = Room. databaseBuilder(
            applicationContext,
            PasswordDatabase::class.java,
            "Passwords"
        )
            .fallbackToDestructiveMigration(false)
            .build()

        cardDatabase = Room.databaseBuilder(
            applicationContext,
            CardDatabase::class.java,
            "Cards"
        )
            .fallbackToDestructiveMigration(false)
            .build()

        isInitialized = true
    }
}