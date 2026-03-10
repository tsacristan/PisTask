package com.example.pistask.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pistask.R
import com.example.pistask.presentation.components.Task
import com.example.pistask.presentation.components.TaskCard
import com.example.pistask.presentation.theme.BleuTurquoise
import com.example.pistask.presentation.theme.VertPistacheClair
import com.example.pistask.presentation.theme.VertPistacheFoncee

// Enum représentant les filtres disponibles
enum class FiltreEtat { TOUTES, A_FAIRE, COMPLETEES }

@Composable
fun HomeScene(tasks: List<Task>, modifier: Modifier = Modifier, onTaskCheck: (Task) -> Unit) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

    // État du filtre sélectionné
    var filtreSelectionne by remember { mutableStateOf(FiltreEtat.TOUTES) }

    // Filtrage + tri
    val filteredTasks = when (filtreSelectionne) {
        FiltreEtat.TOUTES -> tasks.sortedBy { it.isCompleted }
        FiltreEtat.A_FAIRE -> tasks.filter { !it.isCompleted }
        FiltreEtat.COMPLETEES -> tasks.filter { it.isCompleted }
    }

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // ── PARTIE FIXE (non scrollable) ──────────────────────────────

        // En-tête : badge (gauche) + logo (droite)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.droplets),
                        contentDescription = "Eau",
                        modifier = Modifier.size(28.dp),
                        colorFilter = ColorFilter.tint(BleuTurquoise)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "150", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Image(
                    painter = painterResource(id = R.drawable.pistache),
                    contentDescription = "Logo",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop
                )
                Text(text = "Pïstask", color = VertPistacheFoncee, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Top card (résumé)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "GESTIONNAIRE DE TÂCHES", color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$completedCount / $totalCount complétés",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titre "Vos tâches" fixe
        Text(text = "Vos tâches", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        // ── CHIPS DE FILTRE ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                FiltreEtat.TOUTES to "Toutes",
                FiltreEtat.A_FAIRE to "À faire",
                FiltreEtat.COMPLETEES to "Complétées"
            ).forEach { (filtre, label) ->
                val selected = filtreSelectionne == filtre
                FilterChip(
                    selected = selected,
                    onClick = { filtreSelectionne = filtre },
                    label = { Text(text = label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VertPistacheFoncee,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = VertPistacheClair,
                        selectedBorderColor = VertPistacheFoncee
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── LISTE SCROLLABLE ──────────────────────────────────────────
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pistache),
                        contentDescription = "Aucune tâche",
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Pas une seule pistache à décortiquer...\nProfite du calme ou sème la prochaine !",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskCard(task = task, onCheckClick = { onTaskCheck(task) })
                }
            }
        }
    }
}
