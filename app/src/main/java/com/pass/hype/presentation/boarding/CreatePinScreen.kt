@file:Suppress("DEPRECATION")

package com.pass.hype.presentation.boarding

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardPinScreen(
    navController: NavController
    ) {
    var enteredPin by remember { mutableStateOf("") }
    var maxPinLength by remember { mutableIntStateOf(4) }
    var confirmState by remember { mutableStateOf(false) }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var storedPin by remember { mutableStateOf("") }
    val options = listOf("4 Digit", "5 Digit" ,"6 Digit")
    var notMatch by remember { mutableStateOf(false) }

    val vibrator = LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val pinSharedPrefs: SharedPreferences = LocalContext.current.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val onBoardSharedPrefs: SharedPreferences = LocalContext.current.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = if(!confirmState) "Create new PIN for hype.pass" else if (notMatch) "PIN didn't match the previous one" else "Confirm the entered PIN", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground))
            Spacer(modifier = Modifier.size(16.dp))
            if (!confirmState){
                SingleChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            modifier = Modifier.width(100.dp),
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                selectedIndex = index
                                if (selectedIndex == 0) maxPinLength = 4
                                else if(selectedIndex == 1) maxPinLength = 5
                                else maxPinLength = 6
                            },
                            selected = index == selectedIndex
                        ) {
                            Text(label)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(36.dp))

            Row(
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
                                    indication = rememberRipple(color = if (digit == "B" || digit == "F") MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.onBackground)
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
                                                pinSharedPrefs.edit().putInt("pinLength", maxPinLength).apply()
                                                pinSharedPrefs.edit().putString("stored_value", enteredPin).apply()
                                                onBoardSharedPrefs.edit().putString("isUserNew", "No").apply()
                                                navController.popBackStack()
                                                navController.navigate("mainScreen")
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
                                            imageVector = if(!confirmState) Icons.AutoMirrored.Filled.NavigateNext else Icons.Default.Done,
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
        }
    }
}