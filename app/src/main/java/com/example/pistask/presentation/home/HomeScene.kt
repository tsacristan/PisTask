package com.example.pistask.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pistask.presentation.theme.Blanc
import com.example.pistask.presentation.theme.Marron
import com.example.pistask.presentation.theme.VertPistacheClair
import com.example.pistask.presentation.theme.VertPistacheFoncee
import com.example.pistask.presentation.theme.BackgroundCream

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
            // Badge gauche
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BackgroundCream
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.leaf),
                        contentDescription = "Badge",
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "150", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
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

        // Liste d'exemples de cartes
        repeat(3) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Exemple", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}