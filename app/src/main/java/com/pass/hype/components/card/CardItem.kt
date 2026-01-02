package com.pass.hype.components.card

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core. Animatable
import androidx.compose.animation. core.tween
import androidx.compose. animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout. Arrangement
import androidx.compose.foundation. layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation. layout.padding
import androidx.compose.foundation.layout. size
import androidx. compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose. material.icons.filled.Delete
import androidx.compose.material.icons. filled.Edit
import androidx.compose.material. icons.filled. Favorite
import androidx.compose.material.icons. filled.FavoriteBorder
import androidx.compose. material.icons.filled.Lock
import androidx.compose.material.icons. filled.PushPin
import androidx.compose.material.icons. filled.Share
import androidx.compose.material.icons. outlined.PushPin
import androidx. compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose. runtime.LaunchedEffect
import androidx. compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose. runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui. graphics.graphicsLayer
import androidx.compose.ui. graphics.vector.ImageVector
import androidx. compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform. LocalContext
import androidx.compose.ui. unit.dp
import com.pass.hype.components.dialog.DeleteDialog
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room.model. CardSubType
import com. pass.hype. data.room.model.CardType
import com.pass.hype.utils.CardUtils
import kotlinx.coroutines.delay

@Composable
fun CardItem(
    card: Card,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleLock: () -> Unit,
    onCardAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current. nativeClipboard

    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    var showFullNumber by rememberSaveable { mutableStateOf(false) }
    var showCvv by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }
    var showActions by rememberSaveable { mutableStateOf(false) }

    // Auto-hide sensitive data after 10 seconds
    LaunchedEffect(showFullNumber) {
        if (showFullNumber) {
            delay(10000)
            showFullNumber = false
        }
    }

    LaunchedEffect(showCvv) {
        if (showCvv) {
            delay(10000)
            showCvv = false
        }
    }

    LaunchedEffect(Unit) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300))
    }

    // Delete Dialog
    DeleteDialog(
        isOpen = isDeleteDialogOpen,
        item = "Card",
        onDelete = {
            onDelete()
            isDeleteDialogOpen = false
        },
        onDismissRequest = { isDeleteDialogOpen = false }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .graphicsLayer { alpha = alphaAnimation.value }
            .animateContentSize()
    ) {
        // Card Design based on type
        when {
            card.cardSubType == CardSubType. AADHAAR -> {
                AadhaarCardDesign(
                    card = card,
                    showFullNumber = showFullNumber,
                    onToggleVisibility = {
                        showFullNumber = !showFullNumber
                        if (showFullNumber) onCardAccess()
                    }
                )
            }
            card. cardSubType == CardSubType.PAN -> {
                PanCardDesign(
                    card = card,
                    showFullNumber = showFullNumber,
                    onToggleVisibility = {
                        showFullNumber = !showFullNumber
                        if (showFullNumber) onCardAccess()
                    }
                )
            }
            card.cardType == CardType. FINANCIAL -> {
                BankCardDesign(
                    card = card,
                    showFullNumber = showFullNumber,
                    showCvv = showCvv,
                    onToggleNumber = {
                        showFullNumber = ! showFullNumber
                        if (showFullNumber) onCardAccess()
                    },
                    onToggleCvv = {
                        showCvv = ! showCvv
                        if (showCvv) onCardAccess()
                    }
                )
            }
            else -> {
                // Generic card design for other types
                GenericCardDesign(
                    card = card,
                    showFullNumber = showFullNumber,
                    onToggleVisibility = {
                        showFullNumber = !showFullNumber
                        if (showFullNumber) onCardAccess()
                    }
                )
            }
        }

        // Floating Action Buttons
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FloatingActionButton(
                    icon = Icons.Default. Edit,
                    onClick = onEdit,
                    containerColor = Color.White.copy(alpha = 0.95f),
                    contentColor = Color. Black
                )
                FloatingActionButton(
                    icon = if (card.isFavorite) Icons.Default.Favorite else Icons.Default. FavoriteBorder,
                    onClick = onToggleFavorite,
                    containerColor = if (card.isFavorite) Color.Red else Color.White.copy(alpha = 0.95f),
                    contentColor = if (card.isFavorite) Color.White else Color.Black
                )
                FloatingActionButton(
                    icon = if (card.isPinned) Icons.Default.PushPin else Icons. Outlined.PushPin,
                    onClick = onTogglePinned,
                    containerColor = Color.White.copy(alpha = 0.95f),
                    contentColor = Color.Black
                )
                FloatingActionButton(
                    icon = Icons.Default.Lock,
                    onClick = onToggleLock,
                    containerColor = Color.White.copy(alpha = 0.95f),
                    contentColor = Color.Black
                )
                FloatingActionButton(
                    icon = Icons.Default.Share,
                    onClick = {
                        val shareText = buildCardShareText(card)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent. EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Card"))
                    },
                    containerColor = Color.White. copy(alpha = 0.95f),
                    contentColor = Color.Black
                )
                FloatingActionButton(
                    icon = Icons.Default.Delete,
                    onClick = { isDeleteDialogOpen = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FloatingActionButton(
    icon: ImageVector,
    onClick:  () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 4.dp,
        modifier = Modifier. size(32.dp)
    ) {
        Box(contentAlignment = Alignment. Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun buildCardShareText(card: Card): String {
    return buildString {
        appendLine("Card:  ${card.cardName}")
        appendLine("Type: ${CardUtils.getCardSubTypeDisplayName(card.cardSubType)}")
        appendLine("Holder: ${card.holderName}")
        appendLine("Number: ${card.cardNumberMasked}")
        card.expiryDate?.let { appendLine("Expires: $it") }
        card.issuer?.let { appendLine("Issuer: $it") }
        if (card.notes.isNotBlank()) {
            appendLine("Notes:  ${card.notes}")
        }
    }
}