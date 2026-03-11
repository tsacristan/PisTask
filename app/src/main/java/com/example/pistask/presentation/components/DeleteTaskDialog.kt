package com.example.pistask.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.pistask.presentation.components.Task

@Composable
fun DeleteTaskDialog(
    show: Boolean,
    task: Task?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show && task != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Supprimer la tâche ?") },
            text = { Text("Voulez-vous vraiment supprimer la tâche \"${task.title}\" ?") },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Annuler")
                }
            }
        )
    }
}

