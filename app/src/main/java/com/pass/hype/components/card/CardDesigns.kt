package com.pass.hype.components.card

import androidx.compose.foundation. Canvas
import androidx.compose. foundation.background
import androidx.compose.foundation.layout. Arrangement
import androidx.compose.foundation. layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose. foundation.layout.Row
import androidx.compose.foundation.layout. Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation. layout.fillMaxSize
import androidx.compose.foundation.layout. fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout. padding
import androidx. compose.foundation.layout.size
import androidx.compose.foundation. layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.material. icons.Icons
import androidx.compose.material.icons.filled. CreditCard
import androidx.compose.material.icons.filled. Nfc
import androidx. compose.material.icons.filled.Visibility
import androidx.compose.material.icons. filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx. compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose. ui.Alignment
import androidx.compose. ui.Modifier
import androidx.compose. ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry. Size
import androidx.compose.ui.graphics. Brush
import androidx.compose.ui. graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text. font.Font
import androidx.compose.ui.text. font.FontFamily
import androidx.compose.ui.text. font.FontStyle
import androidx.compose.ui.text. font.FontWeight
import androidx.compose. ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import com.pass.hype.R
import com.pass.hype.data.room.model. Card
import com. pass.hype. data.room.model. CardNetwork
import com.pass.hype.data.room.model. CardSubType

// Card Font
val CardFont = FontFamily(
    Font(R. font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold),
    Font(R.font.lato_light, FontWeight.Light)
)

val OcrFont = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal) // You'll need to add this font
)

// ==================== AADHAAR CARD ====================

@Composable
fun AadhaarCardDesign(
    card: Card,
    showFullNumber: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aadhaarOrange = Color(0xFFFF6B00)
    val aadhaarBlue = Color(0xFF1A4B8C)
    val aadhaarLightBlue = Color(0xFF4A90D9)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                . fillMaxSize()
                .background(Color.White)
        ) {
            // Tricolor stripe at top
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Saffron stripe
                drawRect(
                    color = Color(0xFFFF9933),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, 8. dp.toPx())
                )
                // White stripe (already white background)
                // Green stripe
                drawRect(
                    color = Color(0xFF138808),
                    topLeft = Offset(0f, size.height - 8.dp. toPx()),
                    size = Size(size.width, 8.dp. toPx())
                )

                // Ashoka Chakra pattern in background (subtle)
                drawCircle(
                    color = Color(0xFF000080).copy(alpha = 0.03f),
                    radius = size.minDimension * 0.6f,
                    center = Offset(size. width * 0.85f, size.height * 0.5f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment. Top
                ) {
                    // Emblem placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF000080), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🇮🇳",
                            fontSize = 20.sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "भारत सरकार",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000080)
                            )
                        )
                        Text(
                            text = "GOVERNMENT OF INDIA",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF000080)
                            )
                        )
                    }

                    // UIDAI Logo placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(aadhaarOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "आ",
                                color = Color. White,
                                fontWeight = FontWeight. Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "आधार",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = aadhaarOrange
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photo placeholder and details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Photo placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp, 100.dp)
                            .background(Color. LightGray, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = card.holderName. firstOrNull()?.uppercase() ?: "?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color. Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = card.holderName. uppercase(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        card.dateOfBirth?.let { dob ->
                            LabelValue(label = "DOB:", value = dob)
                        }

                        card.gender?.let { gender ->
                            LabelValue(label = "Gender:", value = gender)
                        }
                    }
                }

                Spacer(modifier = Modifier. weight(1f))

                // Aadhaar Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    val displayNumber = if (showFullNumber) {
                        formatAadhaarNumber(card.cardNumber)
                    } else {
                        "XXXX XXXX ${card.cardNumber. takeLast(4)}"
                    }

                    Text(
                        text = displayNumber,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            color = Color.Black,
                            fontFamily = OcrFont
                        )
                    )

                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showFullNumber) Icons.Default. VisibilityOff else Icons.Default. Visibility,
                            contentDescription = "Toggle visibility",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // VID text
                Text(
                    text = "मेरा आधार, मेरी पहचान",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign. Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ==================== PAN CARD ====================

@Composable
fun PanCardDesign(
    card: Card,
    showFullNumber: Boolean,
    onToggleVisibility: () -> Unit,
    modifier:  Modifier = Modifier
) {
    val panYellow = Color(0xFFFFF4CC)
    val panBrown = Color(0xFF8B4513)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                . fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF8DC),
                            Color(0xFFFFF4CC),
                            Color(0xFFFFEBB8)
                        )
                    )
                )
        ) {
            // Decorative pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Corner decorations
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.1f),
                    radius = size.minDimension * 0.3f,
                    center = Offset(0f, 0f)
                )
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.1f),
                    radius = size.minDimension * 0.25f,
                    center = Offset(size.width, size.height)
                )
            }

            Column(
                modifier = Modifier
                    . fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement. SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Emblem
                    Box(
                        modifier = Modifier
                            . size(36.dp)
                            .background(Color(0xFF000080), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🇮🇳", fontSize = 18.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "आयकर विभाग",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = panBrown
                            )
                        )
                        Text(
                            text = "INCOME TAX DEPARTMENT",
                            style = TextStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = panBrown
                            )
                        )
                        Text(
                            text = "GOVT. OF INDIA",
                            style = TextStyle(
                                fontSize = 8.sp,
                                color = panBrown
                            )
                        )
                    }

                    // IT logo placeholder
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(panBrown, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "IT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PAN Title
                Text(
                    text = "PERMANENT ACCOUNT NUMBER CARD",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = panBrown,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Photo and details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Photo placeholder
                    Box(
                        modifier = Modifier
                            .size(70.dp, 85.dp)
                            .background(Color.White, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = card.holderName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Signature placeholder
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(Color. White. copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Signature",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontStyle = FontStyle. Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Name / नाम",
                            style = TextStyle(fontSize = 8.sp, color = Color.Gray)
                        )
                        Text(
                            text = card.holderName. uppercase(),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )

                        card.fatherName?.let { father ->
                            Text(
                                text = "Father's Name",
                                style = TextStyle(fontSize = 8.sp, color = Color.Gray)
                            )
                            Text(
                                text = father. uppercase(),
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier. weight(1f))

                // PAN Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Permanent Account Number",
                            style = TextStyle(fontSize = 8.sp, color = Color.Gray)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showFullNumber) card.cardNumber. uppercase() else maskPanNumber(card.cardNumber),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = Color.Black,
                                    fontFamily = OcrFont
                                )
                            )
                            IconButton(
                                onClick = onToggleVisibility,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (showFullNumber) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle",
                                    tint = Color.Gray,
                                    modifier = Modifier. size(18.dp)
                                )
                            }
                        }
                    }

                    card.dateOfBirth?.let { dob ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Date of Birth",
                                style = TextStyle(fontSize = 8.sp, color = Color.Gray)
                            )
                            Text(
                                text = dob,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== CREDIT/DEBIT CARD ====================

@Composable
fun BankCardDesign(
    card: Card,
    showFullNumber: Boolean,
    showCvv: Boolean,
    onToggleNumber: () -> Unit,
    onToggleCvv: () -> Unit,
    modifier:  Modifier = Modifier
) {
    val gradientColors = getCardGradient(card.cardNetwork)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float. POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color. White. copy(alpha = 0.1f),
                    radius = size.minDimension * 0.7f,
                    center = Offset(size. width * 0.8f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color.White. copy(alpha = 0.08f),
                    radius = size.minDimension * 0.5f,
                    center = Offset(size.width * 0.2f, size.height * 0.9f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    . padding(20.dp)
            ) {
                // Top row - Bank name and card type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        card.issuer?.let { issuer ->
                            Text(
                                text = issuer. uppercase(),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )
                            )
                        }
                        Text(
                            text = when (card.cardSubType) {
                                CardSubType.CREDIT_CARD -> "CREDIT"
                                CardSubType.DEBIT_CARD -> "DEBIT"
                                CardSubType.PREPAID_CARD -> "PREPAID"
                                else -> "CARD"
                            },
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = Color.White. copy(alpha = 0.8f),
                                letterSpacing = 2.sp
                            )
                        )
                    }

                    // Contactless icon
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "Contactless",
                        tint = Color.White. copy(alpha = 0.8f),
                        modifier = Modifier. size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chip
                ChipDesign()

                Spacer(modifier = Modifier.height(16.dp))

                // Card Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showFullNumber) {
                            formatCardNumber(card.cardNumber)
                        } else {
                            formatMaskedCardNumber(card.cardNumber)
                        },
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            letterSpacing = 3.sp,
                            fontFamily = OcrFont
                        )
                    )

                    IconButton(
                        onClick = onToggleNumber,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (showFullNumber) Icons.Default. VisibilityOff else Icons.Default. Visibility,
                            contentDescription = "Toggle",
                            tint = Color.White. copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Cardholder and expiry
                    Column {
                        Text(
                            text = "CARD HOLDER",
                            style = TextStyle(
                                fontSize = 8.sp,
                                color = Color. White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = card.holderName.uppercase(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Expiry
                        card.expiryDate?.let { expiry ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "VALID THRU",
                                    style = TextStyle(
                                        fontSize = 7.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                                Text(
                                    text = expiry,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        // CVV
                        card.cvv?. let { cvv ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CVV",
                                    style = TextStyle(
                                        fontSize = 7.sp,
                                        color = Color.White. copy(alpha = 0.6f)
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (showCvv) cvv else "•••",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight. Medium,
                                            color = Color. White
                                        )
                                    )
                                    IconButton(
                                        onClick = onToggleCvv,
                                        modifier = Modifier. size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showCvv) Icons.Default.VisibilityOff else Icons. Default.Visibility,
                                            contentDescription = "Toggle CVV",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Card Network Logo
                    CardNetworkLogo(network = card.cardNetwork)
                }
            }
        }
    }
}

// ==================== HELPER COMPONENTS ====================

@Composable
private fun ChipDesign() {
    Box(
        modifier = Modifier
            . size(50.dp, 38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD4AF37),
                        Color(0xFFF5E6A3),
                        Color(0xFFD4AF37),
                        Color(0xFFB8962E)
                    )
                )
            )
    ) {
        // Chip lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color(0xFF8B7355)

            // Horizontal lines
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height * 0.33f),
                end = Offset(size.width, size. height * 0.33f),
                strokeWidth = 1.dp. toPx()
            )
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height * 0.66f),
                end = Offset(size.width, size. height * 0.66f),
                strokeWidth = 1.dp.toPx()
            )

            // Vertical lines
            drawLine(
                color = lineColor,
                start = Offset(size.width * 0.33f, 0f),
                end = Offset(size.width * 0.33f, size.height),
                strokeWidth = 1.dp. toPx()
            )
            drawLine(
                color = lineColor,
                start = Offset(size.width * 0.66f, 0f),
                end = Offset(size.width * 0.66f, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun CardNetworkLogo(network: CardNetwork?) {
    when (network) {
        CardNetwork. VISA -> {
            Text(
                text = "VISA",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight. Bold,
                    fontStyle = FontStyle. Italic,
                    color = Color. White
                )
            )
        }
        CardNetwork. MASTERCARD -> {
            Row {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFEB001B), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .offset(x = (-10).dp)
                        .background(Color(0xFFF79E1B).copy(alpha = 0.9f), CircleShape)
                )
            }
        }
        CardNetwork. RUPAY -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RuPay",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
        CardNetwork. AMEX -> {
            Text(
                text = "AMEX",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default. CreditCard,
                contentDescription = "Card",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun LabelValue(label: String, value:  String) {
    Row {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = Color.Gray
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight. Medium,
                color = Color.Black
            )
        )
    }
}

// ==================== UTILITY FUNCTIONS ====================

private fun getCardGradient(network: CardNetwork? ): List<Color> {
    return when (network) {
        CardNetwork.VISA -> listOf(
            Color(0xFF1A1F71),
            Color(0xFF2E3192),
            Color(0xFF1A1F71)
        )
        CardNetwork.MASTERCARD -> listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        )
        CardNetwork. RUPAY -> listOf(
            Color(0xFF007B5F),
            Color(0xFF00A878),
            Color(0xFF007B5F)
        )
        CardNetwork.AMEX -> listOf(
            Color(0xFF006FCF),
            Color(0xFF0080EF),
            Color(0xFF006FCF)
        )
        else -> listOf(
            Color(0xFF2C3E50),
            Color(0xFF3498DB),
            Color(0xFF2C3E50)
        )
    }
}

private fun formatAadhaarNumber(number: String): String {
    val cleaned = number.replace(" ", "").replace("-", "")
    return cleaned.chunked(4).joinToString(" ")
}

private fun maskPanNumber(pan: String): String {
    if (pan.length != 10) return pan
    return "${pan. take(2)}XXXXXX${pan.takeLast(2)}"
}

private fun formatCardNumber(number: String): String {
    val cleaned = number.replace(" ", "").replace("-", "")
    return cleaned.chunked(4).joinToString("  ")
}

private fun formatMaskedCardNumber(number:  String): String {
    val cleaned = number.replace(" ", "").replace("-", "")
    val lastFour = cleaned.takeLast(4)
    return "••••  ••••  ••••  $lastFour"
}

private fun Modifier.offset(x: Dp) = this.then(
    Modifier.padding(start = if (x.value < 0) 0.dp else x, end = if (x.value < 0) (-x. value).dp else 0.dp)
)