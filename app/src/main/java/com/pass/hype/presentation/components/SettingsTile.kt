package com.pass.hype.presentation.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsTile(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    onClick: () -> Unit = {}
) {
    val pinSharedPrefs = LocalContext.current.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val isFingerprintEnabled = pinSharedPrefs.getBoolean("isFingerprintEnabled", false)
    var isFingerprintEnabledCopy by remember { mutableStateOf(isFingerprintEnabled) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick, enabled = title != "Enable Fingerprint"
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (title == "Enable Fingerprint") {
            Switch(checked = isFingerprintEnabledCopy, onCheckedChange = {
                isFingerprintEnabledCopy = it
                pinSharedPrefs.edit().putBoolean("isFingerprintEnabled", it).apply()
            })
        }
    }
}