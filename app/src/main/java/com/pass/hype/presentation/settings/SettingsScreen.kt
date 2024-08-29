package com.pass.hype.presentation.settings

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pass.hype.R
import com.pass.hype.presentation.components.SettingsTile


@Composable
fun SettingsScreen(modifier: Modifier) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    val context = LocalContext.current

    LaunchedEffect(0) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    LazyColumn(modifier = modifier.fillMaxSize().graphicsLayer {
        alpha = alphaAnimation.value
    }, horizontalAlignment = Alignment.CenterHorizontally){
        item {
            Spacer(modifier = Modifier.size(16.dp))
            Card(modifier = Modifier
                .size(160.dp), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Image(painter = painterResource(id = R.drawable.logo), contentDescription = "hype.pass Logo")
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(text = "hype.pass", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.size(10.dp))
            Text(text = "Created with ❤️ by Aryan Srivastava", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.size(16.dp))
            SettingsTile(
                icon = Icons.Default.Password,
                title = "Import/Export Passwords",
                summary = "Import/Export your stored passwords.",
                onClick = {
                    Toast.makeText(context, "Coming Soon", Toast.LENGTH_LONG).show()
                }
            )
            SettingsTile(
                icon = Icons.Default.AddCard,
                title = "Import/Export Cards",
                summary = "Import/Export your stored cards.",
                onClick = {
                    Toast.makeText(context, "Coming Soon", Toast.LENGTH_LONG).show()
                }
            )
            SettingsTile(
                icon = Icons.Default.Code,
                title = "Source Code",
                summary = "View, improve & pull our source code",
                onClick = {

                }
            )

            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

