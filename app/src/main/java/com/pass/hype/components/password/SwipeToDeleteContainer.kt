package com.pass.hype.components.password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx. compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx. compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation. layout.fillMaxSize
import androidx.compose.foundation.layout. padding
import androidx. compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx. compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose. runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime. setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui. graphics.Color
import androidx.compose.ui. res.painterResource
import androidx.compose. ui.unit.dp
import com.pass.hype.R
import kotlinx.coroutines.delay

@Composable
fun SwipeToDeleteContainer(
    item: Any,
    onDelete: () -> Unit,
    animationDuration: Int = 300,
    content: @Composable (SwipeToDismissBoxState) -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue. EndToStart ||
                value == SwipeToDismissBoxValue.StartToEnd) {
                isRemoved = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(animationDuration. toLong())
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = ! isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DeleteBackground(dismissState)
            },
            content = { content(dismissState) },
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true
        )
    }
}

@Composable
private fun DeleteBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme. colorScheme.errorContainer
        SwipeToDismissBoxValue. Settled -> Color. Transparent
    }

    val alignment = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment. CenterEnd
        SwipeToDismissBoxValue. Settled -> Alignment. Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Icon(
            painter = painterResource(R.drawable.delete),
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}