package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.BuyJumpRed

@Composable
fun ConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = title, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = if (isDestructive) {
                        ButtonDefaults.buttonColors(containerColor = BuyJumpRed)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
