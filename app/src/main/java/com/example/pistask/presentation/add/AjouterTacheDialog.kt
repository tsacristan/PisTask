package com.example.pistask.presentation.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjouterTacheDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String?) -> Unit,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDate: String = "",
    initialRecurrence: String = "Quotidien",
    initialPriority: String = "Moyenne",
    initialImageUri: String? = null,
    dialogTitle: String = "NOUVELLE PIS'TÂCHE",
    buttonText: String = "+ AJOUTER une nouvelle pis'tâche"
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf(initialImageUri) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri?.toString()
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val uri = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "TachePhoto", null)
            imageUri = uri
        }
    }

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

    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }
    var time by remember { mutableStateOf("") }

    // Combine date et heure — si pas d'heure, on considère 23:59 pour la validation
    val dateTimeForValidation = if (date.isNotEmpty() && time.isNotEmpty()) "${date} ${time}"
                                else if (date.isNotEmpty()) "${date} 23:59"
                                else ""

    // Vérification date/heure dépassée
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val now = Calendar.getInstance().time
    val isPast = if (dateTimeForValidation.isEmpty()) false else try {
        val dt = sdf.parse(dateTimeForValidation)
        dt != null && dt.before(now)
    } catch (e: Exception) { false }

    val formulaireValide = title.trim().isNotEmpty()
            && date.trim().isNotEmpty()
            && recurrence.trim().isNotEmpty()
            && priority.trim().isNotEmpty()
            && !isPast

    val erreurTitre      = tentativeEnvoi && title.trim().isEmpty()
    val erreurDate       = tentativeEnvoi && date.trim().isEmpty()
    val erreurDatePasse  = tentativeEnvoi && date.trim().isNotEmpty() && isPast
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
                    .fillMaxHeight(0.75f)
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .clickable(enabled = false, onClick = {}),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // ── En-tête FIXE ─────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                HorizontalDivider()

                // ── Contenu SCROLLABLE ────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

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
                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Zone image cliquable ──────────────────────────────────
                    var showImageSourceDialog by remember { mutableStateOf(false) }
                    Text(text = "IMAGE (OPTIONNELLE)", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = imageUri.isNullOrBlank()) { showImageSourceDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUri.isNullOrBlank()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Aucune image sélectionnée",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Touchez pour ajouter une image",
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    val currentUri = imageUri
                                    AsyncImage(
                                        model = if (!currentUri.isNullOrBlank() && currentUri.startsWith("/")) "file://$currentUri" else currentUri,
                                        contentDescription = "Image de la tâche",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { imageUri = null },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Supprimer l'image",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (showImageSourceDialog) {
                        AlertDialog(
                            onDismissRequest = { showImageSourceDialog = false },
                            title = { Text("Ajouter une image") },
                            text = { Text("Choisis comment ajouter l'image.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showImageSourceDialog = false
                                    galleryLauncher.launch("image/*")
                                }) { Text("Galerie") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showImageSourceDialog = false
                                    cameraLauncher.launch(null)
                                }) { Text("Caméra") }
                            }
                        )
                    }
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
                            if (erreurDatePasse) {
                                Text(
                                    text = "La date est déjà passée !",
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
                                } else {
                                    "${date.trim()} 23:59"
                                }
                                onSave(
                                    title.trim(),
                                    description.trim(),
                                    dateFinale,
                                    recurrence.trim(),
                                    priority.trim(),
                                    imageUri
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