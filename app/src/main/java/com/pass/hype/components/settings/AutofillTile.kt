package com.pass.hype.components.settings

import android.content. Context
import android. content.Intent
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillManager
import androidx. compose.foundation.layout.Column
import androidx.compose.foundation.layout. Row
import androidx. compose.foundation.layout.Spacer
import androidx.compose. foundation.layout.fillMaxWidth
import androidx.compose.foundation. layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose. foundation.layout.width
import androidx.compose.foundation.shape. RoundedCornerShape
import androidx. compose.material3.Card
import androidx. compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx. compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime. mutableStateOf
import androidx.compose. runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui. Alignment
import androidx.compose.ui. Modifier
import androidx.compose.ui. platform.LocalContext
import androidx.compose. ui.platform.LocalLifecycleOwner
import androidx. compose.ui.res.painterResource
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui.unit. dp
import androidx. lifecycle.Lifecycle
import androidx.lifecycle. LifecycleEventObserver
import com.pass.hype.R

@Composable
fun AutofillSettingsTile(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isAutofillEnabled by remember { mutableStateOf(false) }

    // Check autofill status on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event. ON_RESUME) {
                isAutofillEnabled = isAutofillServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Initial check
        isAutofillEnabled = isAutofillServiceEnabled(context)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Card(
        modifier = modifier
            . fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAutofillEnabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme. surfaceContainerHigh
            }
        ),
        onClick = {
            openAutofillSettings(context)
        }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.app),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isAutofillEnabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier. width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Autofill Service",
                    style = MaterialTheme. typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAutofillEnabled) {
                        MaterialTheme. colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme. onSurface
                    }
                )
                Text(
                    text = if (isAutofillEnabled) {
                        "HypePass is your autofill provider"
                    } else {
                        "Tap to enable autofill"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAutofillEnabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer. copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Icon(
                painter = painterResource(
                    if (isAutofillEnabled) R.drawable.done else R.drawable.next
                ),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isAutofillEnabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

private fun isAutofillServiceEnabled(context:  Context): Boolean {
    if (Build.VERSION. SDK_INT < Build.VERSION_CODES.O) {
        return false
    }

    val autofillManager = context.getSystemService(AutofillManager::class.java)
    return autofillManager?. hasEnabledAutofillServices() == true
}

private fun openAutofillSettings(context:  Context) {
    if (Build.VERSION. SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
            data = android.net.Uri. parse("package:${context.packageName}")
        }

        try {
            context. startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general autofill settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(fallbackIntent)
            } catch (e:  Exception) {
                // Ignore if settings can't be opened
            }
        }
    }
}