package com.pass.hype.presentation.biometric

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

class BiometricHandler(private val context: Context) {

    private val biometricAuthenticator = BiometricAuthenticator(context)

    fun authenticate(
        fragmentActivity: FragmentActivity,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (Int, CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        biometricAuthenticator.promptBiometricAuth(
            title = "Unlock App",
            subTitle = "Unlock to access Passwords & Cards",
            negativeButtonText = "Use PIN",
            fragmentActivity = fragmentActivity,
            onSuccess = onSuccess,
            onError = onError,
            onFailed = onFailed
        )
    }
}