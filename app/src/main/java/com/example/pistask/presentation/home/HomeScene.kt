package com.example.pistask.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pistask.R
import com.example.pistask.presentation.theme.BleuTurquoise
import com.example.pistask.presentation.theme.VertPistacheFoncee

@Composable
fun HomeScene(modifier: Modifier = Modifier) {
    // HomeScene gère uniquement le contenu — la barre de navigation est gérée par l'activité
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // En-tête : badge (gauche) + logo (droite)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge gauche -> rectangle blanc très arrondi contenant l'icône eau + points
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

            // Logo droite
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
                Text(text = "2 / 3 complétés", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Vos tâches", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        val tasks = listOf(
            com.example.pistask.presentation.components.Task(1, "Réviser le diagramme", "Apprendre les relations UML", "2024-05-20", com.example.pistask.presentation.components.Difficulty.HIGH, 50),
            com.example.pistask.presentation.components.Task(2, "Arroser les plantes", "Remplir l'arrosoir", "2024-05-22", com.example.pistask.presentation.components.Difficulty.MEDIUM, 20),
            com.example.pistask.presentation.components.Task(3, "Nettoyer le jardin", "Ramasser les feuilles", "2024-05-25", com.example.pistask.presentation.components.Difficulty.LOW, 10)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                com.example.pistask.presentation.components.TaskCard(task = task)
            }
        }
    }
}