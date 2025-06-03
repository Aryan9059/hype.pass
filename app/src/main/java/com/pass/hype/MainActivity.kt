package com.pass.hype

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pass.hype.presentation.MainScreen
import com.pass.hype.presentation.biometric.BiometricAuthenticator
import com.pass.hype.presentation.biometric.BiometricScreen
import com.pass.hype.presentation.boarding.OnBoardPinScreen
import com.pass.hype.presentation.boarding.OnBoarding
import com.pass.hype.presentation.cards.CardViewModel
import com.pass.hype.presentation.components.dialog.ChangePinDialog
import com.pass.hype.presentation.passwords.PasswordViewModel
import com.pass.hype.ui.theme.HypepassTheme

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val passwordViewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        val cardViewModel = ViewModelProvider(this)[CardViewModel::class.java]

        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            HypepassTheme {
                val pinSharedPrefs: SharedPreferences =
                    LocalContext.current.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val pinStored = pinSharedPrefs.getString("stored_value", "") ?: ""
                val isFingerPrintEnabled = pinSharedPrefs.getBoolean("isFingerprintEnabled", false)
                val length = pinSharedPrefs.getInt("pinLength" , 0)

                val onBoardSharedPrefs: SharedPreferences =
                    LocalContext.current.getSharedPreferences("onBoarding", MODE_PRIVATE)
                val isUserNew = onBoardSharedPrefs.getString("isUserNew", null)
                var isBoarding by rememberSaveable {
                    mutableStateOf(true)
                }

                var isChangePinDialogOpen by rememberSaveable { mutableStateOf(false) }
                var oldPin by remember { mutableStateOf("") }
                var newPin by remember { mutableStateOf("") }

                ChangePinDialog(
                    isOpen = isChangePinDialogOpen,
                    oldPin = oldPin,
                    newPin = newPin,
                    isStart = true,
                    onDismissRequest = {
                        isChangePinDialogOpen = false
                        oldPin = ""
                        newPin = ""
                    },
                    onOldPinChanged = { oldPin = it.take(length) },
                    onNewPinChanged = { newPin = it.take(length) },
                    onChange = {
                        pinSharedPrefs.edit { putString("stored_value", newPin) }
                        onBoardSharedPrefs.edit { putString("isUserNew", "No") }
                        isBoarding = false
                        isChangePinDialogOpen = false},
                    storedValue = pinStored)

                val navController = rememberNavController()
                var isLock by remember { mutableStateOf(false) }

                if (isLock && isFingerPrintEnabled){
                    val biometricAuthenticator = BiometricAuthenticator(LocalContext.current)
                    biometricAuthenticator.promptBiometricAuth(
                        title = "Unlock App",
                        subTitle = "Unlock to access your passwords",
                        negativeButtonText = "Use PIN",
                        fragmentActivity = LocalView.current.context as FragmentActivity,
                        onSuccess = {
                            navController.popBackStack()
                            navController.navigate("mainScreen")
                        },
                        onError = { _, _ ->

                        },
                        onFailed = {

                        }
                    )
                }

                NavHost(navController = navController, startDestination = if (isUserNew != null) "bioScreen" else "onBoarding"){
                    composable(route = "pinScreen", enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start, tween(400)
                        )
                    }, exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start, tween(400)
                        )
                    }){
                        OnBoardPinScreen(navController)
                    }

                    composable(route = "onBoarding", exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start, tween(400)
                        )
                    }
                    ){
                        Scaffold { paddingValues ->
                            OnBoarding(modifier = Modifier.padding(paddingValues), onClicked = {
                                navController.popBackStack()
                                navController.navigate("pinScreen")
                            }, isBoarding)
                        }
                    }

                    composable(route = "mainScreen", enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start, tween(400)
                        )
                    }){
                        MainScreen(passwordViewModel = passwordViewModel, cardViewModel = cardViewModel)
                    }

                    composable(route = "bioScreen", exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start, tween(400)
                        )
                    }) {
                        isLock = true
                        BiometricScreen(
                            length = length,
                            correctPin = pinStored,
                            isFingerprintEnabled = isFingerPrintEnabled,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}