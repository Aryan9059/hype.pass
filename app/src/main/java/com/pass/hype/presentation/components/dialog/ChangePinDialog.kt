package com.pass.hype.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinDialog(
    isOpen: Boolean,
    isStart: Boolean,
    oldPin: String,
    storedValue: String,
    newPin: String,
    onOldPinChanged: (String) -> Unit,
    onNewPinChanged: (String) -> Unit,
    onChange: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var oldPinError by rememberSaveable { mutableStateOf<String?>(null) }
    var newPinError by rememberSaveable { mutableStateOf<String?>(null) }
    var maxPinLength by remember { mutableIntStateOf(4) }
    val options = listOf("4 Digit", "5 Digit" ,"6 Digit")
    var selectedIndex by remember { mutableIntStateOf(0) }

    oldPinError = if(!isStart) {
        when {
            oldPin != storedValue || oldPin.isBlank() -> "Wrong pin, try again"
            else -> null
        }
    } else {
        when {
            !oldPin.isDigitsOnly() || oldPin.isBlank() || oldPin.length != storedValue.length -> "Please enter a valid pin"
            else -> null
        }
    }

    newPinError = if(!isStart) {
        when {
            !newPin.isDigitsOnly() || oldPin.isBlank() || newPin.length != maxPinLength -> "Please enter a valid pin"
            else -> null
        }
    } else {
        when {
            !newPin.isDigitsOnly() || newPin.isBlank() || newPin != oldPin || newPin.length != maxPinLength -> "Pin doesn't match"
            else -> null
        }
    }

    if (isOpen) {
        AlertDialog(
            title = { Text(text = if (isStart) "Set App Pin" else "Change App Pin") },
            text = {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = oldPin.take(storedValue.length),
                        onValueChange = onOldPinChanged,
                        label = { Text(text = if(isStart) "Enter Pin" else "Old Pin") },
                        singleLine = true,
                        maxLines = 1,
                        isError = oldPinError != null && oldPin.isNotBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = newPin.take(maxPinLength),
                        onValueChange = onNewPinChanged,
                        label = { Text(text = if (isStart) "Confirm Pin" else "New Pin") },
                        singleLine = true,
                        maxLines = 1,
                        isError = newPinError != null && newPin.isNotBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.size(8.dp))
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
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = onChange,
                    enabled = oldPinError == null && newPinError == null
                ) {
                    Text(text = "Apply")
                }
            })
    }
}