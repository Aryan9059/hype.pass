package com.pass.hype.autofill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.autofill.AutofillId
import android.view.autofill. AutofillManager
import android.view.autofill. AutofillValue
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx. activity.compose.setContent
import androidx. biometric.BiometricManager
import androidx. biometric.BiometricPrompt
import androidx. compose.foundation.layout. Arrangement
import androidx. compose.foundation.layout.Column
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx. compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation. layout.height
import androidx.compose.foundation.layout. padding
import androidx. compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx. compose.material3.Button
import androidx. compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx. compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx. compose.runtime. Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime. remember
import androidx.compose.runtime.setValue
import androidx.compose. ui.Alignment
import androidx.compose. ui.Modifier
import androidx.compose. ui.res.painterResource
import androidx.compose.ui. text.font.Font
import androidx.compose.ui.text. font.FontFamily
import androidx.compose.ui.text. input.KeyboardType
import androidx. compose.ui.text.input. PasswordVisualTransformation
import androidx.compose. ui.text.input. VisualTransformation
import androidx.compose. ui.text.style.TextAlign
import androidx.compose.ui.unit. dp
import androidx. core.content. ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pass.hype.R
import com.pass.hype.autofill. builder.ResponseBuilder
import com.pass.hype.ui.theme.HypepassTheme
import java.util.concurrent. Executor

class AutofillAuthActivity : FragmentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var executor: Executor
    private var biometricPrompt: BiometricPrompt? = null

    private var packageNameExtra: String? = null
    private var webDomainExtra: String? = null
    private var usernameId: AutofillId? = null
    private var emailId: AutofillId? = null
    private var passwordId: AutofillId? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("MyPrefs", Context. MODE_PRIVATE)
        executor = ContextCompat.getMainExecutor(this)

        // Extract intent extras
        packageNameExtra = intent.getStringExtra(ResponseBuilder.EXTRA_PACKAGE_NAME)
        webDomainExtra = intent. getStringExtra(ResponseBuilder.EXTRA_WEB_DOMAIN)
        usernameId = intent. getParcelableExtra(ResponseBuilder. EXTRA_USERNAME_ID)
        emailId = intent.getParcelableExtra(ResponseBuilder.EXTRA_EMAIL_ID)
        passwordId = intent.getParcelableExtra(ResponseBuilder. EXTRA_PASSWORD_ID)

        val storedPin = prefs.getString("stored_value", "") ?: ""
        val pinLength = prefs.getInt("pinLength", 4)
        val isFingerprintEnabled = prefs.getBoolean("isFingerprintEnabled", false)

        setContent {
            HypepassTheme {
                AuthScreen(
                    storedPin = storedPin,
                    pinLength = pinLength,
                    isFingerprintEnabled = isFingerprintEnabled,
                    onPinSuccess = { onAuthenticationSuccess() },
                    onBiometricClick = { showBiometricPrompt() },
                    onCancel = { onAuthenticationFailed() }
                )
            }
        }

        // Auto-trigger biometric if enabled
        if (isFingerprintEnabled && isBiometricAvailable()) {
            showBiometricPrompt()
        }
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager. from(this)
        return biometricManager. canAuthenticate(BiometricManager. Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager. BIOMETRIC_SUCCESS
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock HypePass")
            .setSubtitle("Authenticate to autofill credentials")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt. AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt. AuthenticationResult) {
                    super. onAuthenticationSucceeded(result)
                    onAuthenticationSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // User can still use PIN
                }

                override fun onAuthenticationFailed() {
                    super. onAuthenticationFailed()
                    Toast.makeText(
                        this@AutofillAuthActivity,
                        "Authentication failed",
                        Toast. LENGTH_SHORT
                    ).show()
                }
            })

        biometricPrompt?. authenticate(promptInfo)
    }

    private fun onAuthenticationSuccess() {
        HypeAutofillService.setAuthenticated(this)

        // Navigate to selection activity
        val selectionIntent = Intent(this, AutofillSelectionActivity::class.java).apply {
            putExtra(ResponseBuilder.EXTRA_PACKAGE_NAME, packageNameExtra)
            putExtra(ResponseBuilder.EXTRA_WEB_DOMAIN, webDomainExtra)
            putExtra(ResponseBuilder. EXTRA_USERNAME_ID, usernameId)
            putExtra(ResponseBuilder. EXTRA_EMAIL_ID, emailId)
            putExtra(ResponseBuilder.EXTRA_PASSWORD_ID, passwordId)
        }
        startActivityForResult(selectionIntent, REQUEST_CODE_SELECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data:  Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECTION) {
            setResult(resultCode, data)
            finish()
        }
    }

    private fun onAuthenticationFailed() {
        setResult(Activity. RESULT_CANCELED)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_SELECTION = 2001
    }
}

@Composable
private fun AuthScreen(
    storedPin:  String,
    pinLength: Int,
    isFingerprintEnabled: Boolean,
    onPinSuccess: () -> Unit,
    onBiometricClick:  () -> Unit,
    onCancel:  () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier. fillMaxSize(),
        color = MaterialTheme.colorScheme.scrim. copy(alpha = 0.5f)
    ) {
        Card(
            modifier = Modifier
                . padding(32.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Unlock HypePass",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Enter your PIN to autofill credentials",
                    style = MaterialTheme.typography. bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= pinLength) {
                            enteredPin = it
                            error = null
                        }
                        if (it.length == pinLength) {
                            if (it == storedPin) {
                                onPinSuccess()
                            } else {
                                error = "Incorrect PIN"
                                enteredPin = ""
                            }
                        }
                    },
                    label = { Text("PIN") },
                    modifier = Modifier. fillMaxWidth(),
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    visualTransformation = if (pinVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = ! pinVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (pinVisible) R.drawable.eye_close else R.drawable. eye_open
                                ),
                                contentDescription = if (pinVisible) "Hide" else "Show",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    textStyle = androidx.compose.ui. text.TextStyle(
                        fontFamily = FontFamily(Font(R.font.password))
                    )
                )

                if (isFingerprintEnabled) {
                    TextButton(onClick = onBiometricClick) {
                        Icon(
                            painter = painterResource(R.drawable.fingerprint),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Use Fingerprint")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (enteredPin == storedPin) {
                            onPinSuccess()
                        } else {
                            error = "Incorrect PIN"
                            enteredPin = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enteredPin.isNotEmpty()
                ) {
                    Text("Unlock")
                }

                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}