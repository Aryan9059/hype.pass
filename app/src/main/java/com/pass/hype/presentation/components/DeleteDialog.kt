package com.pass.hype.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteDialog(
    isOpen: Boolean,
    item: String,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            title = { Text(text = "Delete $item") },
            text = { Text(text = "Do you really want to delete your saved $item?") },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = onDelete,
                ) {
                    Text(text = "Delete")
                }
            })
    }
}