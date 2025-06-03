package com.pass.hype.presentation.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.pass.hype.presentation.components.SettingsTile
import com.pass.hype.presentation.components.dialog.ChangePinDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    val context = LocalContext.current

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val pinStored = sharedPreferences.getString("stored_value", "") ?: ""

    var isChangePinDialogOpen by rememberSaveable { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

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
                   isChangePinDialogOpen = false},
        storedValue = pinStored)

    LaunchedEffect(0) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .graphicsLayer {
            alpha = alphaAnimation.value
        }, horizontalAlignment = Alignment.CenterHorizontally){
        item {
            MediumTopAppBar(title = { Text(text = "Settings", style = MaterialTheme.typography.headlineLarge) })
            Spacer(Modifier.size(16.dp))

            SettingsTile(
                icon = Icons.Default.Pin,
                title = "Change Pin",
                summary = "Change the security pin of the app",
                onClick = {
                    isChangePinDialogOpen = true
                }
            )

            SettingsTile(
                icon = Icons.Default.Fingerprint,
                title = "Enable Fingerprint",
                summary = "Enable biometric unlock",
                onClick = {}
            )

            SettingsTile(
                icon = Icons.Default.Code,
                title = "Source Code",
                summary = "View, improve & pull our source code",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://github.com/Aryan9059/hype.pass".toUri())
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.size(40.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "Created with ❤️ by ", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Aryan Srivastava", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.size(4.dp))
            Text(text = "Currently using v${context.packageManager.getPackageInfo(context.packageName, 0).versionName}", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline, fontSize = 12.sp))
        }
    }
}


