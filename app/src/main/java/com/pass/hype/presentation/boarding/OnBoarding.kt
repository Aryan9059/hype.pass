package com.pass.hype.presentation.boarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pass.hype.R

@Composable
fun OnBoarding(modifier: Modifier, onClicked: () -> Unit, isBoarding: Boolean) {
    val alphaAnimation = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(100) {
        alphaAnimation.animateTo(targetValue = 1f, animationSpec = tween(300, 0))
    }

    if (isBoarding) {
        Surface(
            modifier = Modifier
                .graphicsLayer { alpha = alphaAnimation.value }
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).paint(painter = painterResource(R.drawable.onboard_bg), contentScale = ContentScale.FillBounds), verticalArrangement = Arrangement.SpaceBetween) {
                Column(modifier.padding(24.dp)) {
                    Spacer(modifier = Modifier.size(64.dp))
                    Image(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)), painter = painterResource(R.drawable.logo), contentDescription = "")
                    Text(
                        modifier = Modifier
                            .padding(top = 20.dp),
                        color = Color.Black,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.password)),
                        text = "passwords.\nmade.\nsecure.",
                        lineHeight = 56.sp
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp),
                        color = Color.Black,
                        text = "This app aims to simplify & secure the way passwords are stored on mobile devices. hype.pass stores your passwords & card details offline.",
                    )
                }
                TextButton(
                    onClick = onClicked,
                    modifier = modifier
                        .height(80.dp)
                        .width(160.dp)
                        .padding(16.dp)
                        .align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors()
                        .copy(containerColor = Color.Black)
                ) {
                    Text(text = "Get Started", color = Color.White)
                }
            }
        }
    }
}