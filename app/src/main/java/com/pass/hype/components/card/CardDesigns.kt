package com.pass.hype.components.card

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pass.hype.data.room.model.Card
import com.pass.hype.data.room.model.CardNetwork
import kotlin.random.Random

val CardFont = FontFamily.SansSerif
val OcrFont = FontFamily.Monospace

// ==================== AADHAAR CARD ====================

@Composable
fun AadhaarCardDesign(
    card: Card,
    showFullNumber: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Standard PVC Aadhaar Aspect Ratio
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Pattern (Subtle Indian Guilloche)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                for (i in 0..size.width.toInt() step 20) {
                    path.moveTo(i.toFloat(), 0f)
                    path.lineTo(i.toFloat() - 100, size.height)
                }
                drawPath(
                    path = path,
                    color = Color(0xFFFFA500).copy(alpha = 0.05f),
                    style = Stroke(width = 1f)
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Header (Government of India)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emblem
                    Text(text = "🇮🇳", fontSize = 24.sp) // Replace with Emblem Image if available

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "भारत सरकार",
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        )
                        Text(
                            text = "GOVERNMENT OF INDIA",
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        )
                    }

                    // Invisible spacer to balance the header or Logo
                    Box(modifier = Modifier.size(24.dp))
                }

                // Decorative Header Line
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFF9933))) // Saffron
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))       // White
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF138808))) // Green
                }

                // 2. Main Content (Photo - Details - QR)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Photo
                    Box(
                        modifier = Modifier
                            .weight(0.28f)
                            .aspectRatio(0.8f)
                            .background(Color.LightGray, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = card.holderName.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Center: Details
                    Column(
                        modifier = Modifier.weight(0.45f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Name (Hindi/English simulation)
                        Text(
                            text = card.holderName,
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // DOB
                        Text(
                            text = "जन्म तिथि / DOB : ${card.dateOfBirth ?: "XX/XX/XXXX"}",
                            style = TextStyle(fontSize = 10.sp, color = Color.Black)
                        )

                        // Gender
                        Text(
                            text = "पुरुष / Male", // Logic to switch based on data
                            style = TextStyle(fontSize = 10.sp, color = Color.Black)
                        )
                    }

                    // Right: QR Code
                    Box(
                        modifier = Modifier
                            .weight(0.27f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FakeQrCode()
                    }
                }

                // 3. Bottom Section (Number)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Number
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (showFullNumber) formatAadhaarNumber(card.cardNumber) else "XXXX XXXX ${card.cardNumber.takeLast(4)}",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (showFullNumber) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Red Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .background(Color(0xFFDB3631)), // Aadhaar Red
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "आधार - आम आदमी का अधिकार",
                            style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

// ==================== PAN CARD (BLUE NSDL STYLE) ====================

@Composable
fun PanCardDesign(
    card: Card,
    showFullNumber: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panBlueStart = Color(0xFFD6E4F1)
    val panBlueEnd = Color(0xFF98B8D6)
    val panTextBlue = Color(0xFF005A9C)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(panBlueStart, panBlueEnd),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            // Background Text Pattern "INCOMETAXDEPARTMENT"
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
                // Drawing complex text patterns is hard in Canvas,
                // using simple lines to simulate the wave texture
                val path = Path()
                for(i in 0..size.height.toInt() step 10) {
                    path.moveTo(0f, i.toFloat())
                    path.cubicTo(
                        size.width * 0.3f, i.toFloat() - 20,
                        size.width * 0.7f, i.toFloat() + 20,
                        size.width, i.toFloat()
                    )
                }
                drawPath(path, color = Color.Black, style = Stroke(width = 0.5f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // 1. Top Row: Emblem and Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "🇮🇳", fontSize = 20.sp) // Emblem

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "आयकर विभाग",
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        )
                        Text(
                            text = "INCOME TAX DEPARTMENT",
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        )
                        Text(
                            text = "GOVT. OF INDIA",
                            style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        )
                    }

                    // Hologram Simulation
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha=0.5f), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Middle Row: Photo, Details, QR
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Photo
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .aspectRatio(0.8f)
                            .background(Color.White, RoundedCornerShape(2.dp))
                            .border(0.5.dp, Color.Black, RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Photo", fontSize = 8.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Details
                    Column(
                        modifier = Modifier.weight(0.5f),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Name
                        Column {
                            Text("Name", fontSize = 6.sp, color = Color.Gray)
                            Text(card.holderName.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Father's Name
                        Column {
                            Text("Father's Name", fontSize = 6.sp, color = Color.Gray)
                            Text(
                                card.fatherName?.uppercase() ?: "FATHER NAME",
                                fontSize = 10.sp, fontWeight = FontWeight.Bold
                            )
                        }

                        // DOB
                        Column {
                            Text("Date of Birth", fontSize = 6.sp, color = Color.Gray)
                            Text(
                                card.dateOfBirth ?: "DD/MM/YYYY",
                                fontSize = 10.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // QR Code
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FakeQrCode()
                    }
                }

                // 3. Bottom Row: PAN Number and Signature
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha=0.5f)).padding(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Signature Space
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(20.dp)
                                .background(Color.White),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("Signature", fontSize = 8.sp, fontStyle = FontStyle.Italic, color = Color.LightGray, modifier = Modifier.padding(start=4.dp))
                        }

                        // Number
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Permanent Account Number",
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = if (showFullNumber) card.cardNumber.uppercase() else maskPanNumber(card.cardNumber),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = OcrFont,
                                    letterSpacing = 1.sp
                                )
                            )
                            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    imageVector = if (showFullNumber) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle",
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
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
    modifier: Modifier = Modifier
) {
    val gradientColors = getCardGradient(card.cardNetwork)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 8.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 600f) // Angled gradient
                    )
                )
        ) {
            // World Map / Noise Pattern
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
                drawCircle(color = Color.White, radius = size.width * 0.6f, center = Offset(size.width, 0f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp) // More breathing room like real cards
            ) {
                // 1. Top Row: Bank & WiFi Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = card.issuer?.uppercase() ?: "BANK NAME",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "Contactless",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp).rotate(90f) // Usually rotated on cards
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Chip & NFC
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChipDesign()
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // 3. Card Number
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showFullNumber) formatCardNumber(card.cardNumber) else formatMaskedCardNumber(card.cardNumber),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = OcrFont,
                            color = Color.White,
                            letterSpacing = 2.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha=0.5f), blurRadius = 2f)
                        )
                    )
                    IconButton(onClick = onToggleNumber, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = if (showFullNumber) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Bottom Row: Details & Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Name and Date Group
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                text = "VALID THRU",
                                fontSize = 6.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = card.expiryDate ?: "MM/YY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontFamily = OcrFont
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = card.holderName.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    // Network Logo
                    CardNetworkLogo(network = card.cardNetwork)
                }
            }
        }
    }
}


// ==================== HELPER COMPONENTS & UTILS ====================

@Composable
fun FakeQrCode() {
    Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        val cellSize = size.width / 10
        for(x in 0..9) {
            for(y in 0..9) {
                // Randomly draw black squares to simulate QR data
                if(Random.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x * cellSize, y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
        // Draw positioning squares (corner boxes)
        val posSize = cellSize * 3
        val corners = listOf(
            Offset(0f, 0f),
            Offset(size.width - posSize, 0f),
            Offset(0f, size.height - posSize)
        )
        corners.forEach { offset ->
            drawRect(color = Color.White, topLeft = offset, size = Size(posSize, posSize))
            drawRect(color = Color.Black, topLeft = offset, size = Size(posSize, posSize), style = Stroke(width = cellSize/2))
            drawRect(color = Color.Black, topLeft = offset + Offset(cellSize, cellSize), size = Size(cellSize, cellSize))
        }
    }
}

// Ensure you have this modifier extension
fun Modifier.rotate(degrees: Float) = this.then(
    Modifier.graphicsLayer(rotationZ = degrees)
)

@Composable
private fun ChipDesign() {
    Box(
        modifier = Modifier
            .size(45.dp, 35.dp) // Standard EMV chip size ratio
            .clip(RoundedCornerShape(4.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE6C87C), Color(0xFFFFF2C2), Color(0xFFD4AF37))
                )
            )
            .border(1.dp, Color(0xFFB8860B).copy(alpha=0.5f), RoundedCornerShape(4.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 1.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            val lineC = Color(0xFF8B6914).copy(alpha=0.6f)

            // Draw Chip Contacts
            val w = size.width
            val h = size.height

            // Center line
            drawLine(lineC, Offset(0f, h/2), Offset(w, h/2), strokeWidth = 1f)
            drawLine(lineC, Offset(w/3, 0f), Offset(w/3, h), strokeWidth = 1f)
            drawLine(lineC, Offset(w*2/3, 0f), Offset(w*2/3, h), strokeWidth = 1f)

            // Rounded internal details
            drawRoundRect(lineC, topLeft = Offset(w/3 + 2f, h/4), size = Size(w/3 - 4f, h/2), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f), style = stroke)
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

// ==================== UTILITY FUNCTIONS ====================

private fun Modifier.offset(x: Dp) = this.then(
    Modifier.padding(start = if (x.value < 0) 0.dp else x, end = if (x.value < 0) (-x. value).dp else 0.dp)
)

private fun getCardGradient(network: CardNetwork?): List<Color> {
    return when (network) {
        CardNetwork.VISA -> listOf(Color(0xFF141E30), Color(0xFF243B55))
        CardNetwork.MASTERCARD -> listOf(Color(0xFF20002c), Color(0xFFcbb4d4))
        CardNetwork.RUPAY -> listOf(Color(0xFF00416A), Color(0xFFE4E5E6)) // RuPay Platinum scheme
        else -> listOf(Color(0xFF232526), Color(0xFF414345))
    }
}

private fun formatAadhaarNumber(number: String): String {
    val cleaned = number.filter { it.isDigit() }
    return cleaned.chunked(4).joinToString(" ")
}

private fun maskPanNumber(pan: String): String {
    if (pan.length < 6) return pan
    return "${pan.take(2)}X XXXXX ${pan.takeLast(2)}"
}

private fun formatCardNumber(number: String): String {
    return number.filter { it.isDigit() }.chunked(4).joinToString("  ")
}

private fun formatMaskedCardNumber(number: String): String {
    val last4 = number.takeLast(4)
    return "••••  ••••  ••••  $last4"
}