@file:Suppress("DEPRECATION")

package com.pass.hype.autofill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.autofill.AutofillId
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pass.hype.R
import com.pass.hype.autofill.builder.ResponseBuilder
import com.pass.hype.ui.theme.HypepassTheme
import com.pass.hype.ui.theme.berlinFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class AutofillAuthActivity : FragmentActivity() {

    private lateinit var prefs: SharedPreferences
    private var biometricPrompt: BiometricPrompt? = null

    private var packageNameExtra: String? = null
    private var webDomainExtra: String? = null
    private var usernameId: AutofillId? = null
    private var emailId: AutofillId? = null
    private var passwordId: AutofillId? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        packageNameExtra = intent.getStringExtra(ResponseBuilder.EXTRA_PACKAGE_NAME)
        webDomainExtra = intent.getStringExtra(ResponseBuilder.EXTRA_WEB_DOMAIN)
        usernameId = intent.getParcelableExtra(ResponseBuilder.EXTRA_USERNAME_ID)
        emailId = intent.getParcelableExtra(ResponseBuilder.EXTRA_EMAIL_ID)
        passwordId = intent.getParcelableExtra(ResponseBuilder.EXTRA_PASSWORD_ID)

        val storedPin = prefs.getString("stored_value", "") ?: ""
        val pinLength = prefs.getInt("pinLength", 4)
        val isFingerprintEnabled = prefs.getBoolean("isFingerprintEnabled", false)
        val biometricAvailable = isBiometricAvailable()

        setContent {
            HypepassTheme {
                AuthScreen(
                    storedPin = storedPin,
                    pinLength = pinLength,
                    showBiometric = isFingerprintEnabled && biometricAvailable,
                    onPinSuccess = { onAuthenticationSuccess() },
                    onBiometricClick = { showBiometricPrompt() },
                    onCancel = { onAuthenticationFailed() }
                )
            }
        }

        if (isFingerprintEnabled && biometricAvailable) {
            showBiometricPrompt()
        }
    }

    private fun isBiometricAvailable(): Boolean {
        return BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock HypePass")
            .setSubtitle("Authenticate to autofill credentials")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticationSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Falls back to PIN entry silently
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )
        biometricPrompt?.authenticate(promptInfo)
    }

    private fun onAuthenticationSuccess() {
        HypeAutofillService.setAuthenticated(this)
        val selectionIntent = Intent(this, AutofillSelectionActivity::class.java).apply {
            putExtra(ResponseBuilder.EXTRA_PACKAGE_NAME, packageNameExtra)
            putExtra(ResponseBuilder.EXTRA_WEB_DOMAIN, webDomainExtra)
            putExtra(ResponseBuilder.EXTRA_USERNAME_ID, usernameId)
            putExtra(ResponseBuilder.EXTRA_EMAIL_ID, emailId)
            putExtra(ResponseBuilder.EXTRA_PASSWORD_ID, passwordId)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(selectionIntent, REQUEST_CODE_SELECTION)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECTION) {
            setResult(resultCode, data)
            finish()
        }
    }

    private fun onAuthenticationFailed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_SELECTION = 2001
    }
}

@Composable
private fun AuthScreen(
    storedPin: String,
    pinLength: Int,
    showBiometric: Boolean,
    onPinSuccess: () -> Unit,
    onBiometricClick: () -> Unit,
    onCancel: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shakeKey by remember { mutableIntStateOf(0) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(shakeKey) {
        if (shakeKey > 0) {
            repeat(3) {
                shakeOffset.animateTo(8f, keyframes { durationMillis = 60 })
                shakeOffset.animateTo(-8f, keyframes { durationMillis = 60 })
            }
            shakeOffset.animateTo(0f, keyframes { durationMillis = 60 })
        }
    }

    fun onDigitPressed(key: String) {
        when (key) {
            "B" -> {
                if (enteredPin.isNotEmpty()) {
                    enteredPin = enteredPin.dropLast(1)
                    errorMessage = null
                }
            }
            else -> {
                if (enteredPin.length < pinLength) {
                    enteredPin += key
                    errorMessage = null
                    if (enteredPin.length == pinLength) {
                        if (enteredPin == storedPin) {
                            onPinSuccess()
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
                                    ?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                            }
                            errorMessage = "Incorrect PIN, try again"
                            enteredPin = ""
                            scope.launch { shakeKey++ }
                        }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Lock icon in primary container circle
            Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.lock),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Unlock HypePass",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = berlinFontFamily,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = errorMessage ?: if (showBiometric) "Use PIN or fingerprint to autofill" else "Enter PIN to autofill",
                style = MaterialTheme.typography.bodyMedium,
                color = if (errorMessage != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(40.dp))

            // PIN dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                repeat(pinLength) { index ->
                    val filled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = when {
                                    filled && errorMessage != null -> MaterialTheme.colorScheme.error
                                    filled -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Numpad
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(if (showBiometric) "F" else " ", "0", "B")
                )

                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { key ->
                            val isSpecial = key == "B" || key == "F"
                            val isEmpty = key == " "

                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .then(
                                        if (!isEmpty) Modifier
                                            .background(
                                                color = if (isSpecial)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                                shape = RoundedCornerShape(50)
                                            )
                                            .clip(RoundedCornerShape(50))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(
                                                    color = if (isSpecial)
                                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                                    else
                                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                                )
                                            ) {
                                                if (key == "F") onBiometricClick()
                                                else onDigitPressed(key)
                                            }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "B" -> Icon(
                                        painter = painterResource(R.drawable.backspace),
                                        contentDescription = "Delete",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .padding(end = 2.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    "F" -> Icon(
                                        painter = painterResource(R.drawable.fingerprint),
                                        contentDescription = "Use Fingerprint",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    " " -> {}
                                    else -> Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
