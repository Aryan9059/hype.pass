@file:Suppress("DEPRECATION")

package com.pass.hype.presentation.biometric

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.pass.hype.R
import com.pass.hype.utils.RecoveryKeyAlertDialog

@Composable
fun BiometricScreen(
    correctPin: String,
    length: Int,
    isFingerprintEnabled: Boolean,
    navController: NavController) {

    var enteredPin by remember { mutableStateOf("") }
    var isPinWrong by remember { mutableStateOf(false) }

    val context = LocalActivity.current as ComponentActivity
    val activity = LocalActivity.current as FragmentActivity
    val biometricAuthenticator = BiometricAuthenticator(context)

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(100) {
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, 0)
        )
    }

    var showRecoveryDialog by rememberSaveable { mutableStateOf(false) }
    if(showRecoveryDialog) {
        RecoveryKeyAlertDialog(context) {
            showRecoveryDialog = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.graphicsLayer { alpha = alphaAnimation.value }.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(modifier = Modifier
                .padding(16.dp)
                .size(36.dp), painter = painterResource(R.drawable.lock), contentDescription = "Lock Icon", tint = MaterialTheme.colorScheme.onBackground)
            Text(text = "Unlock to use hype.pass", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground))
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = if(isPinWrong) "The pin you entered is incorrect, try again" else "Use Pin or Fingerprint to unlock the app", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
            Spacer(modifier = Modifier.size(36.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(length) { index ->
                    val char = if (index < enteredPin.length) "●" else "○"
                    Text(
                        text = char,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val digits = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    if (isFingerprintEnabled) listOf("B", "0", "F") else listOf("0", "B")
                )

                digits.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { digit ->
                            Box(modifier = Modifier
                                .background(
                                    if (digit == "B" || digit == "F") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(50)
                                )
                                .size(86.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    enabled = !(digit == "F" && !isFingerprintEnabled),
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = if (digit == "B" || digit == "F") MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.onBackground)
                                ) {
                                    vibrator.vibrate(
                                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                                    )
                                    when (digit) {
                                        "B" -> if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                        }

                                        "F" -> {
                                            biometricAuthenticator.promptBiometricAuth(
                                                title = "Unlock App",
                                                subTitle = "Unlock to access Passwords & Cards",
                                                negativeButtonText = "Use PIN",
                                                fragmentActivity = activity,
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

                                        else -> if (enteredPin.length < length) {
                                            enteredPin += digit
                                            if (enteredPin.length == length) {
                                                if (enteredPin == correctPin) {
                                                    navController.popBackStack()
                                                    navController.navigate("mainScreen")
                                                } else {
                                                    isPinWrong = true
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                }) {
                                when (digit) {
                                    "B" -> {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.align(Alignment.Center).size(32.dp).padding(end = 2.dp),
                                            painter = painterResource(R.drawable.backspace),
                                            contentDescription = "BackSpace"
                                        )
                                    }
                                    "F" -> {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .align(Alignment.Center),
                                            painter = painterResource(R.drawable.fingerprint),
                                            contentDescription = "Use FingerPrint"
                                        )
                                    }
                                    else -> {
                                        Text(
                                            modifier = Modifier.align(Alignment.Center),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            text = digit,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = 32.sp,
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(onClick = {
                    showRecoveryDialog = true
                },
                    modifier = Modifier
                        .padding(top = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )) {
                    Text(text = "Forgot PIN?")
                }
            }
        }
    }
}