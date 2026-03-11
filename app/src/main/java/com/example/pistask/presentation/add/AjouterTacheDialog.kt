package com.example.pistask.presentation.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjouterTacheDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDate: String = "",
    initialRecurrence: String = "Quotidien",
    initialPriority: String = "Moyenne",
    dialogTitle: String = "NOUVELLE TÂCHE",
    buttonText: String = "+ AJOUTER AU GESTIONNAIRE"
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var date by remember { mutableStateOf(initialDate) }
    var recurrence by remember { mutableStateOf(initialRecurrence) }
    var priority by remember { mutableStateOf(initialPriority) }

    var expandedRecurrence by remember { mutableStateOf(false) }
    val recurrenceOptions = listOf("Unique", "Quotidien", "Hebdomadaire", "Mensuel", "Trimestriel", "Annuel")
    var expandedPriority by remember { mutableStateOf(false) }
    val priorityOptions = listOf("Basse", "Moyenne", "Haute")

    // Déclenchement de la validation
    var tentativeEnvoi by remember { mutableStateOf(false) }

    val formulaireValide = title.trim().isNotEmpty()
            && date.trim().isNotEmpty()
            && recurrence.trim().isNotEmpty()
            && priority.trim().isNotEmpty()

    val erreurTitre      = tentativeEnvoi && title.trim().isEmpty()
    val erreurDate       = tentativeEnvoi && date.trim().isEmpty()
    val erreurRecurrence = tentativeEnvoi && recurrence.trim().isEmpty()
    val erreurPriorite   = tentativeEnvoi && priority.trim().isEmpty()

    // DatePickerDialog natif
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            date = "%02d/%02d/%04d".format(day, month + 1, year)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }
    var time by remember { mutableStateOf("") }

    // Ajout du TimePickerDialog natif
    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            hour = selectedHour
            minute = selectedMinute
            time = "%02d:%02d".format(hour, minute)
            date = if (date.isNotEmpty()) date else "%02d/%02d/%04d".format(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
        },
        hour,
        minute,
        true
    )

    // Combine date et heure
    val dateTime = if (date.isNotEmpty() && time.isNotEmpty()) "${date} ${time}" else date

    // Vérification date/heure dépassée
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
    val now = Calendar.getInstance().time
    val isPast = try {
        val dt = sdf.parse(dateTime)
        dt != null && dt.before(now)
    } catch (e: Exception) { false }

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // ── En-tête ──────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dialogTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Titre ────────────────────────────────────────────────
                    Text(text = "DÉTAILS DE LA TÂCHE", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Ex : Arroser les plantes", color = Color.Gray) },
                        label = { Text("Titre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = erreurTitre,
                        singleLine = true
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

                    // ── Description ──────────────────────────────────────────
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Ex : Penser aux cactus du balcon", color = Color.Gray) },
                        label = { Text("Description (optionnel)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Date + Récurrence ────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Date
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "DATE LIMITE", color = Color.Gray, fontSize = 12.sp)
                            OutlinedTextField(
                                value = date,
                                onValueChange = {},
                                placeholder = { Text("jj/mm/aaaa", color = Color.Gray) },
                                label = { Text("Date") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                readOnly = true,
                                isError = erreurDate || isPast,
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { datePickerDialog.show() }) {
                                            Icon(
                                                Icons.Default.DateRange,
                                                contentDescription = "Choisir une date",
                                                tint = if (erreurDate || isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { timePickerDialog.show() }) {
                                            Icon(
                                                Icons.Default.DateRange,
                                                contentDescription = "Choisir une heure",
                                                tint = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
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
                            if (isPast && date.isNotEmpty() && time.isNotEmpty()) {
                                Text(
                                    text = "Date/heure butoir dépassée !",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        // Récurrence
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "RÉCURRENCE", color = Color.Gray, fontSize = 12.sp)
                            ExposedDropdownMenuBox(
                                expanded = expandedRecurrence,
                                onExpandedChange = { expandedRecurrence = it }
                            ) {
                                OutlinedTextField(
                                    value = recurrence,
                                    onValueChange = {},
                                    placeholder = { Text("Choisir…", color = Color.Gray) },
                                    label = { Text("Récurrence") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    shape = RoundedCornerShape(8.dp),
                                    readOnly = true,
                                    isError = erreurRecurrence,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRecurrence)
                                    }
                                )
                                ExposedDropdownMenu(
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
                            if (erreurRecurrence) {
                                Text(
                                    text = "Une récurrence est nécessaire",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Priorité ─────────────────────────────────────────────
                    Text(text = "PRIORITÉ (IMPACT SUR LES POINTS)", color = Color.Gray, fontSize = 12.sp)
                    ExposedDropdownMenuBox(
                        expanded = expandedPriority,
                        onExpandedChange = { expandedPriority = it }
                    ) {
                        OutlinedTextField(
                            value = priority,
                            onValueChange = {},
                            placeholder = { Text("Choisir…", color = Color.Gray) },
                            label = { Text("Priorité") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            isError = erreurPriorite,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority)
                            }
                        )
                        ExposedDropdownMenu(
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
                    }
                    if (erreurPriorite) {
                        Text(
                            text = "Une priorité est nécessaire",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Bouton ───────────────────────────────────────────────
                    Button(
                        onClick = {
                            if (formulaireValide) {
                                val dateFinale = if (date.isNotEmpty() && time.isNotEmpty()) {
                                    "${date.trim()} ${time.trim()}"
                                } else if (date.isNotEmpty() && !date.contains(":")) {
                                    // Si l'heure n'est pas présente, ajouter minuit
                                    "${date.trim()} 00:00"
                                } else {
                                    date.trim()
                                }
                                onSave(
                                    title.trim(),
                                    description.trim(),
                                    dateFinale,
                                    recurrence.trim(),
                                    priority.trim()
                                )
                            } else {
                                tentativeEnvoi = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
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
                        Text(buttonText)
                    }
                }
            }
        }
    }
}