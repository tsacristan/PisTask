package com.example.pistask.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.pistask.presentation.components.Priorite
import com.example.pistask.presentation.components.Recurrence
import com.example.pistask.presentation.components.Task
import com.example.pistask.presentation.theme.VertPistacheClair
import com.example.pistask.presentation.theme.VertPistacheFoncee
import com.example.pistask.presentation.theme.Orange
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@Composable
fun PrioritePill(priorite: Priorite, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (priorite) {
        Priorite.BASSE -> Pair(VertPistacheClair.copy(alpha = 0.35f), VertPistacheFoncee)
        Priorite.MOYENNE -> Pair(VertPistacheClair.copy(alpha = 0.55f), VertPistacheFoncee)
        Priorite.HAUTE -> Pair(Orange.copy(alpha = 0.25f), Color.Red)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (priorite) {
                Priorite.BASSE -> "BASSE"
                Priorite.MOYENNE -> "MOYENNE"
                Priorite.HAUTE -> "HAUTE"
            },
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onCheckClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
) {
    // Vérification date dépassée : la tâche est en retard seulement après 23h59 le jour J
    val isPast = try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val taskDate = sdf.parse(task.date)
        if (taskDate != null) {
            val cal = java.util.Calendar.getInstance()
            cal.time = taskDate
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            cal.set(java.util.Calendar.SECOND, 59)
            cal.time.before(Date())
        } else false
    } catch (e: Exception) { false }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Checkbox custom
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (task.isCompleted) VertPistacheClair.copy(alpha = 0.7f)
                                else VertPistacheClair.copy(alpha = 0.15f)
                            )
                            .border(2.dp, VertPistacheFoncee, RoundedCornerShape(10.dp))
                            .clickable { onCheckClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = "Complétée", tint = VertPistacheFoncee)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                            )
                            if (isPast && !task.isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "En retard !",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PrioritePill(priorite = task.priorite)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier la tâche")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // séparation grise entre le contenu principal et la ligne metadata
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val recurrenceLabel = when (task.recurrence) {
                        Recurrence.UNIQUE       -> "UNIQUE"
                        Recurrence.QUOTIDIEN    -> "QUOTIDIEN"
                        Recurrence.HEBDOMADAIRE -> "HEBDOMADAIRE"
                        Recurrence.MENSUEL      -> "MENSUEL"
                        Recurrence.TRIMESTRIEL  -> "TRIMESTRIEL"
                        Recurrence.SEMESTRIEL   -> "SEMESTRIEL"
                        Recurrence.ANNUEL       -> "ANNUEL"
                    }

                    // Capsule récurrence
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = recurrenceLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Date butoire
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = task.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Text(
                    text = "+${task.points} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VertPistacheFoncee
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TaskCardPreview() {
    val sample = Task(
        id = 1,
        title = "Réviser le diagramme",
        subtitle = "Apprendre les relations UML",
        recurrence = Recurrence.QUOTIDIEN,
        date = "2024-05-20",
        priorite = Priorite.HAUTE,
        points = 50
    )
    TaskCard(task = sample)
}
