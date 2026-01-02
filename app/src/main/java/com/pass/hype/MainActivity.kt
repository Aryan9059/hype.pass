package com.pass.hype

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.pass.hype.presentation.HypePassApp
import com.pass.hype.presentation.cards.CardViewModel
import com.pass.hype.presentation.passwords.PasswordViewModel

class MainActivity : FragmentActivity() {

    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var cardViewModel: CardViewModel

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModels()
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            HypePassApp(
                window = window,
                passwordViewModel = passwordViewModel,
                cardViewModel = cardViewModel
            )
        }
    }

    private fun initViewModels() {
        passwordViewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        cardViewModel = ViewModelProvider(this)[CardViewModel::class.java]
    }
}