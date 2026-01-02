package com.pass.hype.presentation.settings

import android.content.Context
import android.content. Intent
import android. content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core. Animatable
import androidx. compose.animation.core.tween
import androidx. compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout. Arrangement
import androidx.compose.foundation. layout.Column
import androidx.compose.foundation.layout. Row
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation. layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose. foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx. compose.material3.CircularProgressIndicator
import androidx. compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose. material3.RadioButton
import androidx. compose.material3.Text
import androidx.compose.material3.TextButton
import androidx. compose.material3.TopAppBar
import androidx.compose.runtime. Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose. runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose. runtime.saveable.rememberSaveable
import androidx.compose. runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui. graphics.graphicsLayer
import androidx.compose.ui. platform.LocalContext
import androidx.compose. ui.res.painterResource
import androidx.compose. ui.text.font.Font
import androidx.compose.ui.text. font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import androidx.core.content.edit
import androidx.core. net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pass.hype.R
import com.pass.hype.components. SettingsTile
import com. pass.hype. components.dialog.ChangePinDialog
import com.pass.hype.components.settings.AutofillSettingsTile
import com.pass.hype.presentation.settings.backup.BackupViewModel
import com.pass.hype.utils.RecoveryKeyAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    val context = LocalContext.current

    val backupViewModel: BackupViewModel = viewModel(
        factory = BackupViewModel.provideFactory(context)
    )
    val backupUiState by backupViewModel.uiState.collectAsState()

    val sharedPreferences:  SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context. MODE_PRIVATE)
    val pinStored = sharedPreferences.getString("stored_value", "") ?: ""

    var isChangePinDialogOpen by rememberSaveable { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    var showRecoveryDialog by rememberSaveable { mutableStateOf(false) }
    var showBackupDialog by rememberSaveable { mutableStateOf(false) }
    var showRestoreDialog by rememberSaveable { mutableStateOf(false) }
    var replaceExisting by rememberSaveable { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<android.net.Uri? >(null) }

    // File picker for restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts. OpenDocument()
    ) { uri ->
        uri?.let {
            selectedRestoreUri = it
            showRestoreDialog = true
        }
    }

    // Show toast messages
    LaunchedEffect(backupUiState. message) {
        backupUiState. message?.let { message ->
            Toast. makeText(context, message, Toast.LENGTH_LONG).show()
            backupViewModel.clearMessage()
        }
    }

    LaunchedEffect(0) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    // Dialogs
    if (showRecoveryDialog) {
        RecoveryKeyAlertDialog(context) {
            showRecoveryDialog = false
        }
    }

    ChangePinDialog(
        isOpen = isChangePinDialogOpen,
        oldPin = oldPin,
        newPin = newPin,
        isStart = false,
        onDismissRequest = {
            isChangePinDialogOpen = false
            oldPin = ""
            newPin = ""
        },
        onOldPinChanged = { oldPin = it },
        onNewPinChanged = { newPin = it },
        onChange = {
            sharedPreferences.edit { putInt("pinLength", newPin.length) }
            sharedPreferences.edit { putString("stored_value", newPin) }
            isChangePinDialogOpen = false
            Toast.makeText(context, "PIN changed successfully", Toast.LENGTH_SHORT).show()
        },
        storedValue = pinStored
    )

    // Backup Dialog
    BackupDialog(
        isOpen = showBackupDialog,
        isLoading = backupUiState.isLoading,
        onDismiss = { showBackupDialog = false },
        onConfirm = {
            backupViewModel.createBackup(pinStored)
            showBackupDialog = false
        }
    )

    // Restore Dialog
    RestoreDialog(
        isOpen = showRestoreDialog,
        isLoading = backupUiState.isLoading,
        replaceExisting = replaceExisting,
        onReplaceExistingChanged = { replaceExisting = it },
        onDismiss = {
            showRestoreDialog = false
            selectedRestoreUri = null
        },
        onConfirm = {
            selectedRestoreUri?.let { uri ->
                backupViewModel.restoreBackup(uri, pinStored, replaceExisting)
            }
            showRestoreDialog = false
            selectedRestoreUri = null
        }
    )

    // Loading overlay
    if (backupUiState.isLoading) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Please wait...")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            . fillMaxSize()
            .graphicsLayer { alpha = alphaAnimation.value },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme. typography.headlineLarge,
                        fontFamily = FontFamily(Font(R.font.password))
                    )
                }
            )
            Spacer(Modifier.size(4.dp))

            Image(
                painter = painterResource(R.drawable.banner),
                contentDescription = "Settings Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        MaterialTheme. colorScheme.surfaceVariant,
                        RoundedCornerShape(20.dp)
                    )
                    .graphicsLayer { alpha = alphaAnimation.value }
            )

            Spacer(Modifier.size(12.dp))
            Text(
                "Security",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp)
            )

            AutofillSettingsTile()

            SettingsTile(
                icon = painterResource(R. drawable.pin),
                title = "Change PIN",
                summary = "Change the security PIN of the app",
                onClick = { isChangePinDialogOpen = true }
            )

            SettingsTile(
                icon = painterResource(R. drawable.fingerprint),
                title = "Enable Fingerprint",
                summary = "Enable biometric unlock",
                onClick = { }
            )

            SettingsTile(
                icon = painterResource(R.drawable.forget),
                title = "Forgot PIN",
                summary = "Reset your PIN using the recovery key",
                onClick = { showRecoveryDialog = true }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Spacer(Modifier.size(12.dp))
            Text(
                "Backup & Restore",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    . padding(start = 20.dp)
            )

            SettingsTile(
                icon = painterResource(R. drawable.backup),
                title = "Backup Passwords & Cards",
                summary = "Create a secure backup on your device",
                onClick = { showBackupDialog = true }
            )

            SettingsTile(
                icon = painterResource(R.drawable.restore),
                title = "Restore from Backup",
                summary = "Restore passwords & cards from a backup file",
                onClick = {
                    filePickerLauncher.launch(arrayOf("*/*"))
                }
            )

            HorizontalDivider(Modifier. padding(vertical = 8.dp))
            Spacer(Modifier.size(12.dp))
            Text(
                "Miscellaneous",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    . padding(start = 20.dp)
            )

            SettingsTile(
                icon = painterResource(R. drawable.code),
                title = "Source Code",
                summary = "View, improve & pull our source code",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/Aryan9059/hype.pass". toUri()
                    )
                    context.startActivity(intent)
                }
            )

            SettingsTile(
                icon = painterResource(R.drawable.privacy),
                title = "Privacy Policy",
                summary = "Read the privacy policy of the app",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/Aryan9059/hype.pass/blob/master/sources/privacy_policy.md".toUri()
                    )
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Spacer(modifier = Modifier.size(40.dp))
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement. Center
            ) {
                Text(
                    text = "Created with 💜 by ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Aryan Srivastava",
                    style = MaterialTheme.typography.bodyMedium. copy(fontWeight = FontWeight. Bold)
                )
            }
            Spacer(Modifier.size(4.dp))
            Text(
                text = "Currently using v${
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        0
                    ).versionName
                }",
                style = MaterialTheme.typography.bodyMedium. copy(
                    color = MaterialTheme. colorScheme.outline,
                    fontSize = 12.sp
                )
            )
            Spacer(Modifier.size(16.dp))
        }
    }
}

@Composable
private fun BackupDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    onDismiss:  () -> Unit,
    onConfirm: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Backup your Data") },
            text = {
                Text(
                    "Creates a secure, PIN-encrypted backup of all your passwords and cards.  " +
                            "The backup will be saved to Documents/hype.pass folder."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = !isLoading
                ) {
                    Text("Backup Now")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RestoreDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    replaceExisting: Boolean,
    onReplaceExistingChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Restore from Backup") },
            text = {
                Column {
                    Text("Restore your passwords and cards from the selected backup file.")
                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = ! replaceExisting,
                            onClick = { onReplaceExistingChanged(false) }
                        )
                        Text(
                            "Merge with existing data",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = replaceExisting,
                            onClick = { onReplaceExistingChanged(true) }
                        )
                        Text(
                            "Replace all existing data",
                            modifier = Modifier. padding(start = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = ! isLoading
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}