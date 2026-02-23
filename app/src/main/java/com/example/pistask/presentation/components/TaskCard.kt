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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.pistask.presentation.theme.VertPistacheClair
import com.example.pistask.presentation.theme.VertPistacheFoncee
import com.example.pistask.presentation.theme.Orange

enum class Difficulty { LOW, MEDIUM, HIGH }

data class Task(
    val id: Int,
    val title: String,
    val subtitle: String,
    val date: String,
    val difficulty: Difficulty,
    val points: Int
)

@Composable
fun DifficultyPill(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (difficulty) {
        Difficulty.LOW -> Pair(VertPistacheClair.copy(alpha = 0.35f), VertPistacheFoncee)
        Difficulty.MEDIUM -> Pair(VertPistacheClair.copy(alpha = 0.55f), VertPistacheFoncee)
        Difficulty.HIGH -> Pair(Orange.copy(alpha = 0.25f), Color.Red)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = when (difficulty) {
            Difficulty.LOW -> "BASSE"
            Difficulty.MEDIUM -> "MOYENNE"
            Difficulty.HIGH -> "HAUTE"
        }, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

@Composable
fun TaskCard(task: Task, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Placeholder icon circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VertPistacheClair.copy(alpha = 0.15f))
                            .border(2.dp, VertPistacheFoncee, RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(text = task.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = task.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                DifficultyPill(difficulty = task.difficulty)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // séparation grise entre le contenu principal et la ligne metadata
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // small metadata (date)
                    Text(text = task.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }

                Text(text = "+${task.points} pts", style = MaterialTheme.typography.bodyMedium, color = VertPistacheFoncee)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TaskCardPreview() {
    val sample = Task(1, "Réviser le diagramme", "Apprendre les relations UML", "2024-05-20", Difficulty.HIGH, 50)
    TaskCard(task = sample)
}
