package com.example.pistask.presentation.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pistask.presentation.components.Task
import com.example.pistask.presentation.add.AjouterTacheDialog
import com.example.pistask.presentation.components.Priorite
import com.example.pistask.presentation.components.Recurrence

@Composable
fun ModifierTacheDialog(
    show: Boolean,
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    AjouterTacheDialog(
        show = show,
        onDismiss = onDismiss,
        onSave = { title, description, date, recurrence, priority, imageUri ->
            val updatedTask = task.copy(
                title = title,
                subtitle = description,
                date = date,
                recurrence = Recurrence.valueOf(recurrence.uppercase()),
                priorite = Priorite.valueOf(priority.uppercase()),
                imageUri = imageUri
            )
            onSave(updatedTask)
        },
        initialTitle = task.title,
        initialDescription = task.subtitle,
        initialDate = task.date,
        initialRecurrence = task.recurrence.name,
        initialPriority = task.priorite.name,
        initialImageUri = task.imageUri,
        dialogTitle = "MODIFIER LA TÂCHE",
        buttonText = "MODIFIER LA TÂCHE"
    )
}
