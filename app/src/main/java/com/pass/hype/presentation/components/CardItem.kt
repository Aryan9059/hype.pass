package com.pass.hype.presentation.components

import android.content.Intent
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pass.hype.R
import com.pass.hype.data.local.cards.Cards
import com.pass.hype.presentation.cards.CardViewModel
import kotlinx.coroutines.launch

val LatoFont = FontFamily(
    Font(R.font.lato_black, FontWeight.Black),
    Font(R.font.lato_black_italic, FontWeight.Black, FontStyle.Italic),
    Font(R.font.lato_bold, FontWeight.Bold),
    Font(R.font.lato_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.lato_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.lato_light, FontWeight.Light),
    Font(R.font.lato_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_thin, FontWeight.Thin),
    Font(R.font.lato_thin_italic, FontWeight.Thin, FontStyle.Italic)
)

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    item: Cards
) {
    val vm = viewModel<CardViewModel>()
    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    var isCardDialogOpen by rememberSaveable { mutableStateOf(false) }
    var cardName by remember { mutableStateOf(item.cardHolder) }
    var cardNumber by remember { mutableStateOf(item.cardNumber) }
    var expiryMonth by remember { mutableStateOf(item.expires.substring(0, 2)) }
    var expiryYear by remember { mutableStateOf(item.expires.substring(3, 5)) }
    var cvv by remember { mutableStateOf(item.cvv) }
    var company by remember {
        if (item.cardNumber.first() == '5' || cardNumber.first() == '2') {
            mutableStateOf("MASTERCARD")
        } else if (item.cardNumber.first() == '4') {
            mutableStateOf("VISA")
        } else if (item.cardNumber.first() == '3') {
            mutableStateOf("\uD83C\uDDFA\uD83C\uDDF8 EXPRESS")
        } else {
            mutableStateOf("OTHER")
        }
    }

    AddCardDialog(
        isOpen = isCardDialogOpen,
        cardName = cardName,
        cardNumber = cardNumber,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        cvv = cvv,
        company = company,
        onDismissRequest = {
            isCardDialogOpen = false
            cardName = item.cardHolder
            cardNumber = item.cardNumber
            expiryMonth = item.expires.substring(0, 1)
            expiryYear = item.expires.substring(3, 4)
            cvv = item.cvv
            company = (if (cardNumber.isNotBlank()){
                if (cardNumber.first() == '5' || cardNumber.first() == '2') {
                    "MASTERCARD"
                } else if (cardNumber.first() == '4') {
                    "VISA"
                } else if (cardNumber.first() == '3') {
                    "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS"
                } else {
                    "OTHER"
                }
            } else {
                Log.i("","")
            }).toString()
        },
        onConfirmButtonClick = {
            vm.deleteCard(item.cardId)
            vm.addCard(
                Cards(
                    baseColor = item.baseColor,
                    cardNumber = cardNumber,
                    cardHolder = cardName,
                    expires = "$expiryMonth/$expiryYear",
                    cvv = cvv
                )
            )
            isCardDialogOpen = false
            cardName = ""
            cardNumber = ""
            expiryMonth = ""
            expiryYear = ""
            cvv = ""
            company = ""
        },
        onNameChanged = {cardName = it},
        onNumberChanged = {
            cardNumber = it.take(16)
            if (cardNumber.isNotBlank()){
                company = if (cardNumber.first() == '5' || cardNumber.first() == '2') {
                    "MASTERCARD"
                } else if (cardNumber.first() == '4') {
                    "VISA"
                } else if (cardNumber.first() == '3') {
                    "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS"
                } else {
                    "OTHER"
                }
            }
        },
        onExpiryMonthChanged = {expiryMonth = it.take(2)},
        onExpiryYearChanged = {expiryYear = it.take(2)},
        onCvvChanged = {cvv = it.take(3)},
        onCompanyChanged = {company = it},
        edit = true
    )

    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DeleteDialog(isOpen = isDeleteDialogOpen, item = "Card", onDelete = {
        scope.launch {
            vm.deleteCard(item.cardId)
            isDeleteDialogOpen = false
        }
    }, onDismissRequest = { isDeleteDialogOpen = false })

    LaunchedEffect(100) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }
    val bankCardAspectRatio = 1.586f
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 10.dp)
            // Aspect Ratio in Compose
            .aspectRatio(bankCardAspectRatio)
            .graphicsLayer {
                alpha = alphaAnimation.value
            }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current

            BankCardBackground(baseColor = Color(item.baseColor.toColorInt()))
            BankCardNumber(cardNumber = item.cardNumber)
            SpaceWrapper(
                modifier = Modifier.align(Alignment.TopStart),
                space = 32.dp,
                top = true,
                left = true
            ) {
                BankCardLabelAndText(label = "card holder", text = item.cardHolder, clipboardManager)
            }
            SpaceWrapper(
                modifier = Modifier.align(Alignment.BottomStart),
                space = 32.dp,
                bottom = true,
                left = true
            ) {
                Row {
                    BankCardLabelAndText(label = "expires", text = item.expires, clipboardManager)
                    Spacer(modifier = Modifier.width(16.dp))
                    BankCardLabelAndText(label = "cvv", text = item.cvv, clipboardManager)
                }
            }
            SpaceWrapper(
                modifier = Modifier.align(Alignment.BottomEnd),
                space = 32.dp,
                bottom = true,
                right = true
            ) {
                Text(
                    text = if (item.cardNumber.first().toString() == "2" || item.cardNumber.first().toString() == "5") {
                        "MASTERCARD"
                    } else if (item.cardNumber.first().toString() == "4"){
                        "VISA"
                    } else if (item.cardNumber.first().toString() == "3"){
                        "\uD83C\uDDFA\uD83C\uDDF8 EXPRESS"
                    } else "OTHER",
                    style = TextStyle(
                        fontFamily = LatoFont,
                        fontWeight = FontWeight.W500,
                        fontStyle = FontStyle.Italic,
                        fontSize = 22.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                )
            }
            OutlinedCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onError),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .padding(end = 40.dp),
                onClick = {
                    isDeleteDialogOpen = true
                },
                shape = RoundedCornerShape(50)
            ) {
                Icon(modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp),
                    tint = MaterialTheme.colorScheme.error,
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Card")
            }
            OutlinedCard(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .padding(top = 40.dp),
                onClick = {
                    isCardDialogOpen = true
                },
                shape = RoundedCornerShape(50)
            ) {
                Icon(modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp),
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Card")
            }
            OutlinedCard(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, "Holder Name: ${item.cardHolder}\nCard Number: ${item.cardNumber}\nExpires on: ${item.expires}\nCVV: ${item.cvv}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(context, shareIntent, null)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .padding(end = 40.dp, top = 40.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp),
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Card Details")
            }
            OutlinedCard(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                onClick = { clipboardManager.setText(AnnotatedString(item.cardNumber)) },
                shape = RoundedCornerShape(50)
            ) {
                Icon(modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp),
                    imageVector = Icons.Default.CopyAll,
                    contentDescription = "Copy Card Number")
            }
        }
    }
}

@Composable
fun BankCardBackground(baseColor: Color) {
    val colorSaturation75 = baseColor.setSaturation(0.75f)
    val colorSaturation50 = baseColor.setSaturation(0.5f)
    // Drawing Shapes with Canvas
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // Drawing Circles
        drawCircle(
            color = colorSaturation75,
            center = Offset(x = size.width * 0.2f, y = size.height * 0.6f),
            radius = size.minDimension * 0.85f
        )
        drawCircle(
            color = colorSaturation50,
            center = Offset(x = size.width * 0.1f, y = size.height * 0.3f),
            radius = size.minDimension * 0.75f
        )
    }
}

fun Color.toHsl(): FloatArray {
    val redComponent = red
    val greenComponent = green
    val blueComponent = blue

    val maxComponent = maxOf(redComponent, greenComponent, blueComponent)
    val minComponent = minOf(redComponent, greenComponent, blueComponent)
    val delta = maxComponent - minComponent
    val lightness = (maxComponent + minComponent) / 2

    val hue: Float
    val saturation: Float

    if (maxComponent == minComponent) {
        // Grayscale color, no saturation and hue is undefined
        hue = 0f
        saturation = 0f
    } else {
        // Calculating saturation
        saturation = if (lightness > 0.5) delta / (2 - maxComponent - minComponent) else delta / (maxComponent + minComponent)
        // Calculating hue
        hue = when (maxComponent) {
            redComponent -> 60 * ((greenComponent - blueComponent) / delta % 6)
            greenComponent -> 60 * ((blueComponent - redComponent) / delta + 2)
            else -> 60 * ((redComponent - greenComponent) / delta + 4)
        }
    }

    // Returning HSL values, ensuring hue is within 0-360 range
    return floatArrayOf(hue.coerceIn(0f, 360f), saturation, lightness)
}

fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val chroma = (1 - kotlin.math.abs(2 * lightness - 1)) * saturation
    val secondaryColorComponent = chroma * (1 - kotlin.math.abs((hue / 60) % 2 - 1))
    val matchValue = lightness - chroma / 2

    var red = matchValue
    var green = matchValue
    var blue = matchValue

    when ((hue.toInt() / 60) % 6) {
        0 -> { red += chroma; green += secondaryColorComponent }
        1 -> { red += secondaryColorComponent; green += chroma }
        2 -> { green += chroma; blue += secondaryColorComponent }
        3 -> { green += secondaryColorComponent; blue += chroma }
        4 -> { red += secondaryColorComponent; blue += chroma }
        5 -> { red += chroma; blue += secondaryColorComponent }
    }

    // Creating a color from RGB components
    return Color(red = red, green = green, blue = blue)
}

fun Color.setSaturation(newSaturation: Float): Color {
    val hslValues = this.toHsl()
    // Adjusting the saturation while keeping hue and lightness the same
    return hslToColor(hslValues[0], newSaturation.coerceIn(0f, 1f), hslValues[2])
}

@Composable
fun BankCardNumber(cardNumber: String) {

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        repeat(3) {
            BankCardDotGroup()
        }

        Text(
            text = cardNumber.takeLast(4),
            style = TextStyle(
                fontFamily = LatoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 1.sp,
                color = Color.White
            )
        )
    }
}

@Composable
fun BankCardDotGroup() {
    Canvas(
        modifier = Modifier.width(48.dp),
        onDraw = { // You can adjust the width as needed
            val dotRadius = 4.dp.toPx()
            val spaceBetweenDots = 8.dp.toPx()
            for (i in 0 until 4) { // Draw four dots
                drawCircle(
                    color = Color.White,
                    radius = dotRadius,
                    center = Offset(
                        x = i * (dotRadius * 2 + spaceBetweenDots) + dotRadius,
                        y = center.y
                    )
                )
            }
        }
    )
}

@Composable
fun BankCardLabelAndText(label: String, text: String, clipboardManager: ClipboardManager) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .clickable {
                clipboardManager.setText(AnnotatedString(text))
            },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = LatoFont,
                fontWeight = FontWeight.W300,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = LatoFont,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                color = Color.White
            )
        )
    }
}

@Composable
fun SpaceWrapper(
    modifier: Modifier = Modifier,
    space: Dp,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (top) Modifier.padding(top = space) else Modifier)
            .then(if (right) Modifier.padding(end = space) else Modifier)
            .then(if (bottom) Modifier.padding(bottom = space) else Modifier)
            .then(if (left) Modifier.padding(start = space) else Modifier)
    ) {
        content()
    }
}