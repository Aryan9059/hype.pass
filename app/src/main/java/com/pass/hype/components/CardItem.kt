package com.pass.hype.components

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx. compose.animation.animateContentSize
import androidx.compose.animation.core. Animatable
import androidx.compose.animation. core.LinearOutSlowInEasing
import androidx.compose.animation.core. tween
import androidx. compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose. foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose. foundation.layout.Column
import androidx.compose.foundation.layout. Row
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation. layout.fillMaxSize
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose. foundation.layout.padding
import androidx.compose.foundation.layout. size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled. ContentCopy
import androidx.compose.material. icons.filled.Delete
import androidx.compose.material. icons.filled.Edit
import androidx.compose.material. icons.filled. Favorite
import androidx. compose.material.icons.filled.FavoriteBorder
import androidx.compose. material. icons.filled.Lock
import androidx.compose.material.icons. filled.LockOpen
import androidx.compose.material. icons.filled.PushPin
import androidx.compose.material.icons. filled.Share
import androidx.compose.material.icons. filled. Visibility
import androidx.compose.material.icons. filled.VisibilityOff
import androidx.compose.material. icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx. compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime. Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose. runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose. runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui. graphics.graphicsLayer
import androidx.compose.ui. platform.LocalClipboard
import androidx.compose.ui.platform. LocalContext
import androidx.compose.ui. text.TextStyle
import androidx. compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import androidx.core.graphics.toColorInt
import com.pass.hype.components.dialog.DeleteDialog
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room. model.CardType
import com.pass. hype.utils.CardUtils
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun CardItem(
    card: Card,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite:  () -> Unit,
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

    val bankCardAspectRatio = 1.586f

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .aspectRatio(bankCardAspectRatio)
            .graphicsLayer { alpha = alphaAnimation. value }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(20.dp),
        onClick = { showActions = ! showActions }
    ) {
        Box {
            // Background
            CardBackground(baseColor = Color(card.baseColor.toColorInt()))

            // Locked Overlay
            if (card.isLocked) {
                LockedCardOverlay(onUnlock = onToggleLock)
            } else {
                // Card Content
                CardContent(
                    card = card,
                    showFullNumber = showFullNumber,
                    showCvv = showCvv,
                    onToggleNumber = {
                        showFullNumber = !showFullNumber
                        if (showFullNumber) onCardAccess()
                    },
                    onToggleCvv = {
                        showCvv = !showCvv
                        if (showCvv) onCardAccess()
                    },
                    onCopyNumber = {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText("Card Number", card.cardNumber)
                        )
                    }
                )

                // Action Buttons
                androidx.compose.animation.AnimatedVisibility(
                    visible = showActions,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    CardActionButtons(
                        card = card,
                        onEdit = onEdit,
                        onDelete = { isDeleteDialogOpen = true },
                        onToggleFavorite = onToggleFavorite,
                        onTogglePinned = onTogglePinned,
                        onToggleLock = onToggleLock,
                        onShare = {
                            val shareText = buildCardShareText(card)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Card"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedCardOverlay(onUnlock: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color. Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default. Lock,
                contentDescription = "Locked",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Card Locked",
                color = Color.White,
                style = MaterialTheme.typography. bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                onClick = onUnlock,
                shape = RoundedCornerShape(20.dp),
                color = Color.White. copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tap to Unlock",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CardContent(
    card:  Card,
    showFullNumber: Boolean,
    showCvv: Boolean,
    onToggleNumber: () -> Unit,
    onToggleCvv: () -> Unit,
    onCopyNumber:  () -> Unit
) {
    Column(
        modifier = Modifier
            . fillMaxSize()
            .padding(20.dp)
    ) {
        // Top Row - Card Info & Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.cardName,
                    style = TextStyle(
                        fontWeight = FontWeight. Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = CardUtils.getCardSubTypeDisplayName(card.cardSubType),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White. copy(alpha = 0.7f)
                    )
                )
                card.issuer?.let { issuer ->
                    Text(
                        text = issuer,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = Color. White.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (card.isPinned) {
                    CardBadge(icon = Icons.Default. PushPin)
                }
                if (card.isFavorite) {
                    CardBadge(icon = Icons. Default.Favorite, tint = Color.Red)
                }

                // Expiry Status
                card.expiryDate?.let { expiry ->
                    when {
                        CardUtils.isExpired(expiry) -> {
                            ExpiryBadge(text = "EXPIRED", color = Color.Red)
                        }
                        CardUtils.isExpiringSoon(expiry) -> {
                            val days = CardUtils.getDaysUntilExpiry(expiry)
                            ExpiryBadge(
                                text = "${days}d left",
                                color = Color(0xFFFFA000)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Card Number Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showFullNumber) {
                    CardUtils.formatCardNumber(card. cardNumber)
                } else {
                    CardUtils.formatCardNumber(card.cardNumberMasked)
                },
                style = TextStyle(
                    fontWeight = FontWeight. Bold,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    color = Color. White
                ),
                modifier = Modifier.weight(1f)
            )

            Row {
                SmallIconButton(
                    icon = if (showFullNumber) Icons.Default. VisibilityOff else Icons.Default. Visibility,
                    onClick = onToggleNumber,
                    contentDescription = if (showFullNumber) "Hide" else "Show"
                )
                SmallIconButton(
                    icon = Icons.Default.ContentCopy,
                    onClick = onCopyNumber,
                    contentDescription = "Copy"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Holder Name
            CardLabelValue(label = "HOLDER", value = card.holderName. uppercase())

            // Financial Card specific fields
            if (card.cardType == CardType. FINANCIAL) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    card.expiryDate?.let { expiry ->
                        CardLabelValue(label = "EXPIRES", value = expiry)
                    }

                    card.cvv?.let { cvv ->
                        Column {
                            Text(
                                text = "CVV",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (showCvv) cvv else "•••",
                                    style = TextStyle(
                                        fontWeight = FontWeight. Medium,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                )
                                SmallIconButton(
                                    icon = if (showCvv) Icons.Default.VisibilityOff else Icons.Default. Visibility,
                                    onClick = onToggleCvv,
                                    contentDescription = if (showCvv) "Hide CVV" else "Show CVV",
                                    size = 16.dp
                                )
                            }
                        }
                    }
                }
            }

            // Government ID specific fields
            if (card.cardType == CardType.GOVERNMENT_ID) {
                card.dateOfBirth?.let { dob ->
                    CardLabelValue(label = "DOB", value = dob)
                }
                card.validUntil?.let { valid ->
                    CardLabelValue(label = "VALID UNTIL", value = valid)
                }
            }

            // Card Network
            card.cardNetwork?.let { network ->
                Text(
                    text = network. name,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle. Italic,
                        fontSize = 18.sp,
                        color = Color. White
                    )
                )
            }
        }
    }
}

@Composable
private fun CardActionButtons(
    card:  Card,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleLock: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier. padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ActionButton(
            icon = Icons.Default. Edit,
            onClick = onEdit,
            containerColor = Color.White.copy(alpha = 0.9f),
            contentColor = Color.Black
        )
        ActionButton(
            icon = if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            onClick = onToggleFavorite,
            containerColor = if (card.isFavorite) Color.Red else Color.White.copy(alpha = 0.9f),
            contentColor = if (card. isFavorite) Color.White else Color.Black
        )
        ActionButton(
            icon = if (card. isPinned) Icons.Default.PushPin else Icons. Outlined.PushPin,
            onClick = onTogglePinned,
            containerColor = Color.White. copy(alpha = 0.9f),
            contentColor = Color.Black
        )
        ActionButton(
            icon = Icons.Default.Lock,
            onClick = onToggleLock,
            containerColor = Color.White. copy(alpha = 0.9f),
            contentColor = Color.Black
        )
        ActionButton(
            icon = Icons.Default.Share,
            onClick = onShare,
            containerColor = Color. White.copy(alpha = 0.9f),
            contentColor = Color.Black
        )
        ActionButton(
            icon = Icons.Default.Delete,
            onClick = onDelete,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui. graphics.vector.ImageVector,
    onClick: () -> Unit,
    containerColor:  Color,
    contentColor: Color
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.size(32.dp)
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

@Composable
private fun CardBadge(
    icon: androidx.compose.ui. graphics.vector.ImageVector,
    tint: Color = Color.White
) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.2f),
        modifier = Modifier. size(24.dp)
    ) {
        Box(contentAlignment = Alignment. Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ExpiryBadge(text: String, color:  Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight. Bold,
                color = Color.White
            )
        )
    }
}

@Composable
private fun CardLabelValue(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontWeight = FontWeight. Medium,
                fontSize = 14.sp,
                color = Color.White
            )
        )
    }
}

@Composable
private fun SmallIconButton(
    icon: androidx. compose.ui.graphics.vector.ImageVector,
    onClick:  () -> Unit,
    contentDescription: String,
    size: androidx.compose.ui. unit. Dp = 20.dp
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier. size(28.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color. White. copy(alpha = 0.8f),
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun CardBackground(baseColor: Color) {
    val colorSaturation75 = baseColor.adjustSaturation(0.9f)
    val colorSaturation50 = baseColor. adjustSaturation(0.6f)

    Canvas(
        modifier = Modifier
            . fillMaxSize()
            .background(baseColor)
    ) {
        drawCircle(
            color = colorSaturation50,
            center = Offset(x = size.width * 0.2f, y = size.height * 0.6f),
            radius = size.minDimension * 0.85f
        )
        drawCircle(
            color = colorSaturation75,
            center = Offset(x = size.width * 0.1f, y = size.height * 0.3f),
            radius = size.minDimension * 0.75f
        )
    }
}

// Color extension for saturation adjustment
private fun Color.adjustSaturation(factor: Float): Color {
    val hsl = toHsl()
    return hslToColor(hsl[0], (hsl[1] * factor).coerceIn(0f, 1f), hsl[2])
}

private fun Color.toHsl(): FloatArray {
    val r = red
    val g = green
    val b = blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val l = (max + min) / 2f

    val h:  Float
    val s: Float

    if (delta == 0f) {
        h = 0f
        s = 0f
    } else {
        s = if (l > 0.5f) delta / (2f - max - min) else delta / (max + min)
        h = when (max) {
            r -> 60f * ((g - b) / delta % 6f)
            g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }
    }

    return floatArrayOf(h. coerceIn(0f, 360f), s, l)
}

private fun hslToColor(h: Float, s:  Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f

    var r = m
    var g = m
    var b = m

    when ((h. toInt() / 60) % 6) {
        0 -> { r += c; g += x }
        1 -> { r += x; g += c }
        2 -> { g += c; b += x }
        3 -> { g += x; b += c }
        4 -> { r += x; b += c }
        5 -> { r += c; b += x }
    }

    return Color(r, g, b)
}

private fun buildCardShareText(card: Card): String {
    return buildString {
        appendLine("Card:  ${card.cardName}")
        appendLine("Type: ${CardUtils.getCardSubTypeDisplayName(card.cardSubType)}")
        appendLine("Holder: ${card. holderName}")
        appendLine("Number: ${card.cardNumberMasked}")
        card.expiryDate?.let { appendLine("Expires: $it") }
        card.issuer?.let { appendLine("Issuer: $it") }
        if (card.notes.isNotBlank()) {
            appendLine("Notes:  ${card.notes}")
        }
    }
}