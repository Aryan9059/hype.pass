package com.pass.hype.components.password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pass.hype.components.dialog.DeleteDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SwipeToDeleteContainer(
    item: Any,
    onDelete: () -> Unit,
    animationDuration: Int = 300,
    content: @Composable () -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                // Trigger the dialog instead of immediate deletion
                showDeleteDialog = true
                false // Return false to prevent the dismiss from completing immediately visually
            } else {
                false
            }
        }
    )

    // Handle the actual deletion effect
    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            onDelete()
        }
    }

    if (showDeleteDialog) {
        DeleteDialog(
            isOpen = showDeleteDialog,
            item = "Card", // You can pass a specific name if 'item' has one
            onDelete = {
                showDeleteDialog = false
                isRemoved = true
            },
            onDismissRequest = {
                showDeleteDialog = false
                // IMPORTANT: Snap back to original position if cancelled
                coroutineScope.launch {
                    dismissState.reset()
                }
            }
        )
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = { DeleteBackground(dismissState) },
            content = { content() },
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true
        )
    }
}

@Composable
private fun DeleteBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }

    val alignment = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}