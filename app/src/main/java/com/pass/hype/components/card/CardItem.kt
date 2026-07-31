package com.pass.hype.components.card

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room.model.CardSubType
import com.pass.hype.data.room.model.CardType
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
    var showFullNumber by rememberSaveable { mutableStateOf(false) }
    var showCvv by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showCardPhoto by rememberSaveable { mutableStateOf(false) }

    val hasPhoto = !card.frontImageUri.isNullOrEmpty()

    // Auto-hide sensitive data
    LaunchedEffect(showFullNumber) {
        if (showFullNumber) { delay(10000); showFullNumber = false }
    }
    LaunchedEffect(showCvv) {
        if (showCvv) { delay(10000); showCvv = false }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize()
    ) {
        // --- Card visual: photo OR designed card ---
        Box(modifier = Modifier.clickable { onCardAccess() }) {
            if (showCardPhoto && hasPhoto) {
                CardPhotoView(
                    frontImageUri = card.frontImageUri,
                    backImageUri = card.backImageUri
                )
            } else {
                when {
                    card.cardSubType == CardSubType.AADHAAR -> {
                        AadhaarCardDesign(
                            card = card,
                            showFullNumber = showFullNumber,
                            onToggleVisibility = {
                                showFullNumber = !showFullNumber
                                if (showFullNumber) onCardAccess()
                            }
                        )
                    }
                    card.cardSubType == CardSubType.PAN -> {
                        PanCardDesign(
                            card = card,
                            showFullNumber = showFullNumber,
                            onToggleVisibility = {
                                showFullNumber = !showFullNumber
                                if (showFullNumber) onCardAccess()
                            }
                        )
                    }
                    card.cardType == CardType.FINANCIAL -> {
                        BankCardDesign(
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
                            }
                        )
                    }
                    else -> {
                        BankCardDesign(
                            card = card,
                            showFullNumber = showFullNumber,
                            showCvv = showCvv,
                            onToggleNumber = { showFullNumber = !showFullNumber },
                            onToggleCvv = { showCvv = !showCvv }
                        )
                    }
                }
            }
        }

        // --- Action overlay (top-right) ---
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Favorite toggle
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (card.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (card.isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // More options menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    // View card photo / designed card toggle
                    if (hasPhoto) {
                        DropdownMenuItem(
                            text = { Text(if (showCardPhoto) "Show Card Design" else "Show Card Photo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (showCardPhoto) Icons.Default.CreditCard else Icons.Default.Image,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                showCardPhoto = !showCardPhoto
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Edit Card") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { showMenu = false; onEdit() }
                    )

                    DropdownMenuItem(
                        text = { Text(if (card.isPinned) "Unpin" else "Pin to Top") },
                        leadingIcon = {
                            Icon(
                                if (card.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = null
                            )
                        },
                        onClick = { showMenu = false; onTogglePinned() }
                    )

                    DropdownMenuItem(
                        text = { Text("Share Details") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            val shareText = buildCardShareText(card)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Card"))
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }

        // --- Locked indicator (top-left) ---
        if (card.isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    .clickable { onToggleLock() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun buildCardShareText(card: Card): String {
    return buildString {
        appendLine("Card: ${card.cardName}")
        appendLine("Type: ${CardUtils.getCardSubTypeDisplayName(card.cardSubType)}")
        appendLine("Holder: ${card.holderName}")
        appendLine("Number: ${card.cardNumberMasked}")
        card.expiryDate?.let { appendLine("Expires: $it") }
        card.issuer?.let { appendLine("Issuer: $it") }
    }
}
