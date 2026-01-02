package com.pass.hype.components.card

import androidx.compose.foundation. background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout. Column
import androidx. compose.foundation.layout.Row
import androidx.compose.foundation. layout.Spacer
import androidx.compose.foundation.layout. aspectRatio
import androidx.compose.foundation. layout.fillMaxSize
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout. padding
import androidx. compose.foundation.layout.size
import androidx.compose.foundation. shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled. Badge
import androidx.compose.material.icons. filled.Visibility
import androidx.compose.material.icons. filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx. compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose. ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics. Brush
import androidx. compose.ui.graphics.Color
import androidx.compose.ui. text.TextStyle
import androidx. compose.ui.text.font.FontWeight
import androidx.compose.ui.unit. dp
import androidx. compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.pass.hype.data.room.model. Card
import com. pass.hype. utils.CardUtils

@Composable
fun GenericCardDesign(
    card:  Card,
    showFullNumber: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = try {
        Color(card.baseColor.toColorInt())
    } catch (e: Exception) {
        Color(0xFF2C3E50)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            baseColor,
                            baseColor.copy(alpha = 0.8f),
                            baseColor.copy(alpha = 0.9f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float. POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    Column {
                        Text(
                            text = card.cardName,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = CardUtils.getCardSubTypeDisplayName(card.cardSubType),
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color. White.copy(alpha = 0.7f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White. copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Card Number/ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    Text(
                        text = if (showFullNumber) card.cardNumber else card.cardNumberMasked,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    )

                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showFullNumber) Icons.Default. VisibilityOff else Icons.Default. Visibility,
                            contentDescription = "Toggle visibility",
                            tint = Color.White. copy(alpha = 0.8f),
                            modifier = Modifier. size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier. height(12.dp))

                // Bottom Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment. Bottom
                ) {
                    Column {
                        Text(
                            text = "HOLDER",
                            style = TextStyle(
                                fontSize = 8.sp,
                                color = Color. White.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = card.holderName. uppercase(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight. Medium,
                                color = Color.White
                            )
                        )
                    }

                    card.validUntil?.let { validUntil ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "VALID UNTIL",
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    color = Color. White.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = validUntil,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    card.issuer?.let { issuer ->
                        Text(
                            text = issuer.uppercase(),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight. Bold,
                                color = Color.White. copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }
        }
    }
}