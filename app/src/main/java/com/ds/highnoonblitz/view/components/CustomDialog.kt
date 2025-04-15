package com.ds.highnoonblitz.view.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun CustomDialog(
    showDialogState: MutableState<Boolean>,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialogState.value) {
        AlertDialog(
            onDismissRequest = { showDialogState.value = false },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        showDialogState.value = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onDismiss()
                        showDialogState.value = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}