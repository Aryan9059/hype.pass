package com.pass.hype.presentation.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pass.hype.R

@Composable
fun RecoveryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Icon(modifier = Modifier
                .padding(12.dp)
                .size(36.dp), painter = painterResource(R.drawable.recovery), contentDescription = "Lock Icon", tint = MaterialTheme.colorScheme.onBackground)
            Text(text = "Remember Recovery Key", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground))
            Spacer(modifier = Modifier.size(6.dp))
            Text(modifier = Modifier.padding(horizontal = 32.dp), text = "You need to remember this recovery key since it is needed in case you forget your PIN.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
            Spacer(modifier = Modifier.size(36.dp))

            Card {
                Text(text = "AAAA-BBBB-CCCC",
                     style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                     modifier = Modifier.padding(16.dp),
                     textAlign = TextAlign.Center
                )
            }
        }
    }
}