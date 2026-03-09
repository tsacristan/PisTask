
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
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("Quotidien") }
    var priority by remember { mutableStateOf("Moyenne") }

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
                        shape = RoundedCornerShape(8.dp)
                    )
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
                                trailingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = "Date")
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "RÉCURRENCE", color = Color.Gray, fontSize = 12.sp)
                            OutlinedTextField(
                                value = recurrence,
                                onValueChange = { recurrence = it },
                                label = { Text("Quotidien") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "PRIORITÉ (IMPACT SUR LES POINTS)", color = Color.Gray, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { priority = "Basse" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (priority == "Basse") MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text("BASSE")
                        }
                        Button(
                            onClick = { priority = "Moyenne" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (priority == "Moyenne") MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text("MOYENNE")
                        }
                        Button(
                            onClick = { priority = "Haute" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (priority == "Haute") MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text("HAUTE")
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onSave(title.trim(), description.trim()) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("+ AJOUTER AU GESTIONNAIRE", color = Color.White)
                    }
                }
            }
        }
    }
}
