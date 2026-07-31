package com.pass.hype.components.card

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun CardPhotoView(
    frontImageUri: String?,
    backImageUri: String?,
    modifier: Modifier = Modifier
) {
    val hasBack = !backImageUri.isNullOrEmpty()
    var showBack by rememberSaveable { mutableStateOf(false) }

    val currentUri = if (showBack && hasBack) backImageUri else frontImageUri

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .animateContentSize()
    ) {
        AsyncImage(
            model = currentUri?.let { File(it) },
            contentDescription = if (showBack) "Card back" else "Card front",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        )

        // Side label (top-left)
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.45f)
        ) {
            Text(
                text = if (showBack) "Back" else "Front",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Flip button (bottom-right) — only if a back photo exists
        if (hasBack) {
            FilledTonalIconButton(
                onClick = { showBack = !showBack },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.45f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = if (showBack) "Show front" else "Show back"
                )
            }
        }
    }
}
