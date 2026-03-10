package com.example.pistask.presentation.add

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

@Composable
fun AjouterTacheDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("Quotidien") }
    var priority by remember { mutableStateOf("Moyenne") }

    var expandedRecurrence by remember { mutableStateOf(false) }
    val recurrenceOptions = listOf("Quotidien", "Hebdomadaire", "Mensuel", "Trimestriel", "Annuel")
    var expandedPriority by remember { mutableStateOf(false) }
    val priorityOptions = listOf("Basse", "Moyenne", "Haute")

    // Déclenchement de la validation (seulement après une tentative d'envoi)
    var tentativeEnvoi by remember { mutableStateOf(false) }

    val formulaireValide = title.trim().isNotEmpty() && date.trim().isNotEmpty()

    val erreurTitre = tentativeEnvoi && title.trim().isEmpty()
    val erreurDate = tentativeEnvoi && date.trim().isEmpty()

    AnimatedVisibility(
        visible = show,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis = 300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false, onClick = {}),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NOUVELLE TÂCHE",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "DÉTAILS DE LA TÂCHE", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titre...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = erreurTitre
                    )
                    if (erreurTitre) {
                        Text(
                            text = "Un titre est nécessaire",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optionnel)...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "DATE LIMITE", color = Color.Gray, fontSize = 12.sp)
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("13/02/2026") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                isError = erreurDate,
                                trailingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = "Date")
                                }
                            )
                            if (erreurDate) {
                                Text(
                                    text = "Une date est nécessaire",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "RÉCURRENCE", color = Color.Gray, fontSize = 12.sp)
                            OutlinedTextField(
                                value = recurrence,
                                onValueChange = {},
                                label = { Text(recurrence) },
                                modifier = Modifier.fillMaxWidth().clickable { expandedRecurrence = true },
                                shape = RoundedCornerShape(8.dp),
                                readOnly = true,
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Choisir la récurrence")
                                }
                            )
                            DropdownMenu(
                                expanded = expandedRecurrence,
                                onDismissRequest = { expandedRecurrence = false }
                            ) {
                                recurrenceOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            recurrence = option
                                            expandedRecurrence = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "PRIORITÉ (IMPACT SUR LES POINTS)", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        label = { Text(priority) },
                        modifier = Modifier.fillMaxWidth().clickable { expandedPriority = true },
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Choisir la priorité")
                        }
                    )
                    DropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorityOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    priority = option
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (formulaireValide) {
                                onSave(title.trim(), description.trim(), date.trim(), recurrence.trim(), priority.trim())
                            } else {
                                tentativeEnvoi = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formulaireValide)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            contentColor = if (formulaireValide)
                                Color.White
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Text("+ AJOUTER AU GESTIONNAIRE")
                    }
                }
            }
        }
    }
}