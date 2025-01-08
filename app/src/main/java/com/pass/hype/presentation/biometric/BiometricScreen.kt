@file:Suppress("DEPRECATION")

package com.pass.hype.presentation.biometric

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun BiometricScreen(
    correctPin: String,
    length: Int,
    isFingerprintEnabled: Boolean,
    navController: NavController) {

    var enteredPin by remember { mutableStateOf("") }
    var isPinWrong by remember { mutableStateOf(false) }

    val context = LocalContext.current as ComponentActivity
    val activity = LocalContext.current as FragmentActivity
    val biometricAuthenticator = BiometricAuthenticator(context)

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(100) {
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, 0)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.graphicsLayer { alpha = alphaAnimation.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(modifier = Modifier
                .padding(16.dp)
                .size(36.dp), imageVector = Icons.Default.Lock, contentDescription = "Lock Icon", tint = MaterialTheme.colorScheme.onBackground)
            Text(text = "Unlock to use hype.pass", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground))
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = if(isPinWrong) "The pin you entered is incorrect, try again" else "Use Pin or Fingerprint to unlock the app", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground))
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
                            color = MaterialTheme.colorScheme.secondary
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
                                    indication = rememberRipple(color = if (digit == "B" || digit == "F") MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.onBackground)
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
                                                subTitle = "Unlock to access your passwords",
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
                                            modifier = Modifier.align(Alignment.Center),
                                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                            contentDescription = "BackSpace"
                                        )
                                    }
                                    "F" -> {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .align(Alignment.Center),
                                            imageVector = Icons.Default.Fingerprint,
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
            }
        }
    }
}