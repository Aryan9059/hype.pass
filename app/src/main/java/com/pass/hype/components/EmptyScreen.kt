package com.pass.hype.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.pass.hype.R

@Composable
fun EmptyScreen(modifier: Modifier, screenText: String) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(0) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    Box(modifier = modifier
    .fillMaxSize()
    .graphicsLayer {
        alpha = alphaAnimation.value
    },
    contentAlignment = Alignment.Center) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.empty_dark else R.drawable.empty_light),
                contentDescription = "No Passwords Saved",
                modifier = Modifier.size(196.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "It's all empty here!", fontFamily = FontFamily(Font(R.font.password)), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = screenText, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
        }
    }
}