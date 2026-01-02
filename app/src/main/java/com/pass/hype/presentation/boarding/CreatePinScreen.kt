@file:Suppress("DEPRECATION")

package com.pass.hype.presentation.boarding

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import androidx.navigation.NavController
import com.pass.hype.R
import com.pass.hype.utils.generateStrongRecoveryKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardPinScreen(navController: NavController) {
    var enteredPin by remember { mutableStateOf("") }
    var maxPinLength by remember { mutableIntStateOf(4) }
    var confirmState by remember { mutableStateOf(false) }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var storedPin by remember { mutableStateOf("") }
    val options = listOf("4 Digit", "5 Digit" ,"6 Digit")
    var notMatch by remember { mutableStateOf(false) }

    val vibrator = LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    var isFingerprintEnabled by remember { mutableStateOf(true) }

    val pinSharedPrefs: SharedPreferences = LocalContext.current.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val onBoardSharedPrefs: SharedPreferences = LocalContext.current.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
    val recoveryKey = rememberSaveable { generateStrongRecoveryKey() }
    var enteredRecoveryKey by remember { mutableStateOf("") }

    var showRecoveryScreen by remember { mutableStateOf(false) }
    if (showRecoveryScreen) {
        AlertDialog(
            onDismissRequest = { showRecoveryScreen = false },
            title = { Text(text = "Recovery Key") },
            properties = DialogProperties(dismissOnClickOutside = false),
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(modifier = Modifier.fillMaxWidth(), text = "You need to remember this recovery key since it is needed in case you forget your PIN.", textAlign = TextAlign.Start, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.8f)))
                    Spacer(modifier = Modifier.size(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(text = recoveryKey,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            fontFamily = FontFamily(Font(R.font.password)),
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        value = enteredRecoveryKey,
                        onValueChange = { enteredRecoveryKey = it },
                        label = { Text(text = "Confirm Key") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = enteredRecoveryKey != recoveryKey,
                        )
                }
                   },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRecoveryScreen = false
                        pinSharedPrefs.edit {
                            putInt(
                                "pinLength",
                                maxPinLength
                            )
                        }
                        pinSharedPrefs.edit {
                            putString(
                                "stored_value",
                                enteredPin
                            )
                        }
                        pinSharedPrefs.edit {
                            putBoolean(
                                "isFingerprintEnabled",
                                isFingerprintEnabled
                            )
                        }
                        onBoardSharedPrefs.edit {
                            putString(
                                "isUserNew",
                                "No"
                            )
                        }
                        onBoardSharedPrefs.edit {
                            putString(
                                "RecoveryKey",
                                recoveryKey
                            )
                        }
                        navController.popBackStack()
                        navController.navigate("mainScreen")
                    },
                    enabled = enteredRecoveryKey == recoveryKey
                ) {
                    Text(text = "Done")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            Text(text = if(!confirmState) "Create new PIN for hype.pass" else if (notMatch) "PIN didn't match the previous one" else "Confirm the entered PIN", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground))
            Spacer(modifier = Modifier.size(16.dp))
            if (!confirmState){
                SingleChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            modifier = Modifier.width(100.dp),
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size,
                                baseShape = RoundedCornerShape(12.dp)
                            ),
                            onClick = {
                                selectedIndex = index
                                maxPinLength = if (selectedIndex == 0) 4
                                else if(selectedIndex == 1) 5
                                else 6
                            },
                            selected = index == selectedIndex
                        ) {
                            Text(label)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(24.dp))

            if (isFingerprintAvailable(LocalContext.current)) {
                Card(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row {
                        Text(text = "Enable Fingerprint", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterVertically).padding(start = 20.dp))
                        Spacer(Modifier.weight(1F))
                        Switch(modifier = Modifier.padding(vertical = 12.dp).padding(end = 20.dp), checked = isFingerprintEnabled, onCheckedChange = {
                            isFingerprintEnabled = it
                        })
                    }
                }
            }

            Row(
                Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(maxPinLength) { index ->
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
                    listOf("B", "0", "F")
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

                                        "F" -> if (!confirmState && enteredPin.length == maxPinLength) {
                                            confirmState = true
                                            storedPin = enteredPin
                                            enteredPin = ""
                                        } else if (confirmState && enteredPin.length == maxPinLength){
                                            Log.e("storedPin", storedPin)
                                            if (storedPin == enteredPin){
                                                showRecoveryScreen = true
                                            } else {
                                                notMatch = true
                                                enteredPin = ""
                                            }
                                        }

                                        else -> if (enteredPin.length < maxPinLength) {
                                            enteredPin += digit
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
                                            painter = if(!confirmState) painterResource(R.drawable.next) else painterResource(R.drawable.done),
                                            contentDescription = "Next"
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
            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

fun isFingerprintAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            true
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            false
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            false
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            false
        }
        else -> false
    }
}