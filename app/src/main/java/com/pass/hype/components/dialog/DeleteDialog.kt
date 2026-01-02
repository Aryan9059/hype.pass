package com.pass.hype.components.dialog

import androidx.compose. material3.AlertDialog
import androidx. compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose. runtime.Composable

@Composable
fun DeleteDialog(
    isOpen: Boolean,
    item: String,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = "Delete $item?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this $item?  This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDelete
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme. colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        )
    }
}