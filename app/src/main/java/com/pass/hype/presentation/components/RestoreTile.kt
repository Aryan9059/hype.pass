package com.pass.hype.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pass.hype.R
import com.pass.hype.presentation.settings.importDatabase
import kotlinx.coroutines.launch

@Composable
fun RestoreTile(
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var isPinDialogVisible by rememberSaveable { mutableStateOf(false) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                importDatabase(
                    context = context,
                    sourceUri = selectedUri,
                    onSuccess = {
                        onSuccess()
                    },
                    onError = { error ->
                        onError(error)
                    },
                    pinStored = enteredPin
                )
                enteredPin = ""
            }
        }
    }

    if(isPinDialogVisible) {
        PinVerificationDialog(
            onDismiss = {
                isPinDialogVisible = false
                enteredPin = ""
            },
            onPinConfirmed = { pin ->
                enteredPin = pin.trim()
                documentPickerLauncher.launch(
                    arrayOf(
                        "application/x-sqlite3",
                        "application/octet-stream",
                        "*/*"
                    )
                )

                isPinDialogVisible = false
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    isPinDialogVisible = true
                }
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.restore),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Restore Database",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Restore your database from a backup file.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

    }
}

@Composable
fun PinVerificationDialog(
    onDismiss: () -> Unit,
    onPinConfirmed: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Data") },
        text = {
            Column {
                Text("Please enter the app PIN you used when creating the backup. This is needed to restore your data from the backup file.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Enter PIN") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPinConfirmed(pin) },
                enabled = pin.isNotEmpty()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}