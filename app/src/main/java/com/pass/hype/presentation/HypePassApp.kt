package com.pass.hype.presentation

import android. view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx. compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime. remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose. runtime.setValue
import androidx.compose.ui. platform.LocalContext
import androidx.compose. ui.platform.LocalView
import androidx.fragment.app.FragmentActivity
import androidx. navigation.compose.rememberNavController
import com.pass.hype.components.dialog.ChangePinDialog
import com.pass.hype.data.local.PreferencesManager
import com.pass.hype.navigation.NavGraph
import com. pass.hype. navigation.Screen
import com.pass.hype.presentation.biometric.BiometricHandler
import com. pass.hype. presentation.cards.CardViewModel
import com.pass.hype.presentation.passwords.PasswordViewModel
import com.pass.hype.ui.theme.HypepassTheme

@Composable
fun HypePassApp(
    window: Window?,
    passwordViewModel:  PasswordViewModel,
    cardViewModel: CardViewModel
) {
    HypepassTheme {
        val context = LocalContext.current
        val preferencesManager = remember { PreferencesManager(context) }

        val navController = rememberNavController()
        var isLock by remember { mutableStateOf(false) }
        var isBoarding by rememberSaveable { mutableStateOf(true) }

        // Change PIN Dialog State
        var isChangePinDialogOpen by rememberSaveable { mutableStateOf(false) }
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }

        // Secure window flag
        SecureWindowEffect(window)

        // Change PIN Dialog
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
            onOldPinChanged = { oldPin = it. take(preferencesManager.pinLength) },
            onNewPinChanged = { newPin = it.take(preferencesManager.pinLength) },
            onChange = {
                preferencesManager.updatePin(newPin)
                preferencesManager.setUserOnboarded()
                isBoarding = false
                isChangePinDialogOpen = false
            },
            storedValue = preferencesManager.storedPin
        )

        // Biometric Authentication
        if (isLock && preferencesManager.isFingerprintEnabled) {
            val fragmentActivity = LocalView.current.context as FragmentActivity
            val biometricHandler = remember { BiometricHandler(context) }

            biometricHandler.authenticate(
                fragmentActivity = fragmentActivity,
                onSuccess = { _ ->
                    navController.popBackStack()
                    navController.navigate(Screen.MainScreen.route)
                },
                onError = { _, _ -> },
                onFailed = { }
            )
        }

        // Navigation
        NavGraph(
            navController = navController,
            startDestination = if (! preferencesManager. isUserNew) Screen.BioScreen. route else Screen.OnBoarding.route,
            passwordViewModel = passwordViewModel,
            cardViewModel = cardViewModel,
            isBoarding = isBoarding,
            pinLength = preferencesManager.pinLength,
            storedPin = preferencesManager. storedPin,
            isFingerprintEnabled = preferencesManager.isFingerprintEnabled,
            onBioScreenEntered = { isLock = true },
            onOnBoardingClicked = {
                navController. popBackStack()
                navController.navigate(Screen.PinScreen.route)
            }
        )
    }
}

@Composable
private fun SecureWindowEffect(window: Window?) {
    DisposableEffect(Unit) {
        window?. setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams. FLAG_SECURE
        )
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}