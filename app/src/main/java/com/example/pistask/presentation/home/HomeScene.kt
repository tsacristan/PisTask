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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pistask.R
import com.example.pistask.presentation.components.DeleteTaskDialog
import com.example.pistask.presentation.components.Task
import com.example.pistask.presentation.components.TaskCard
import com.example.pistask.presentation.components.WateringCanView
import com.example.pistask.presentation.components.WaterFlowView
import androidx.compose.runtime.LaunchedEffect
import com.example.pistask.presentation.components.dateApparition
import com.example.pistask.presentation.components.recurrenceGenereProchaine
import com.example.pistask.presentation.components.tacheEstVisible
import com.example.pistask.presentation.theme.BleuTurquoise
import com.example.pistask.presentation.theme.VertKaki
import com.example.pistask.presentation.theme.VertPistacheClair
import com.example.pistask.presentation.theme.VertPistacheFoncee
import com.example.pistask.presentation.theme.Orange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Enum représentant les filtres disponibles
enum class FiltreEtat { TOUTES, A_FAIRE, EN_RETARD, REALISEE }

@Composable
fun HomeScene(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    totalPoints: Int = 0,
    dailyPoints: Int = 0,
    onTaskCheck: (Task) -> Unit,
    onEditRequest: (Task) -> Unit,
    onAutoReset: (List<Task>) -> Unit = {},
    onTaskDelete: (Task) -> Unit = {},
    onWateringCanFull: () -> Unit = {}
) {
    // État du filtre sélectionné
    var filtreSelectionne by remember { mutableStateOf(FiltreEtat.TOUTES) }

    // Helper : tâche en retard = deadline 23h59 du jour J dépassée ET non complétée
    fun isEnRetard(task: Task): Boolean {
        if (task.isCompleted) return false
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val taskDate = sdf.parse(task.date) ?: return false
            val cal = java.util.Calendar.getInstance()
            cal.time = taskDate
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            cal.set(java.util.Calendar.SECOND, 59)
            cal.time.before(Date())
        } catch (_: Exception) { false }
    }

    fun doitSafficher(task: Task): Boolean {
        if (task.isCompleted) return true  // toujours visible dans "Réalisée" / "Toutes"
        return isEnRetard(task) || tacheEstVisible(task)
    }

    LaunchedEffect(tasks) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val toReset = tasks.filter { task ->
            task.isCompleted &&
            recurrenceGenereProchaine(task.recurrence) &&
            task.nextDate.isNotEmpty() &&
            run {
                // Reset quand l'apparition de nextDate est atteinte ET pas le jour même du check
                val apparitionStr = sdf.format(dateApparition(task.nextDate, task.recurrence))
                val nextTask = task.copy(date = task.nextDate)
                tacheEstVisible(nextTask) && apparitionStr != todayStr
            }
        }
        if (toReset.isNotEmpty()) {
            onAutoReset(toReset)
        }
    }

    // Filtrage + tri
    val filteredTasks = when (filtreSelectionne) {
        FiltreEtat.TOUTES -> tasks.filter { doitSafficher(it) }
            .sortedWith(compareBy(
                // 0 = en retard, 1 = à faire, 2 = réalisée
                { task -> when {
                    isEnRetard(task)   -> 0
                    !task.isCompleted  -> 1
                    else               -> 2
                }}
            ))
        FiltreEtat.A_FAIRE   -> tasks.filter { !it.isCompleted && !isEnRetard(it) && tacheEstVisible(it) }.sortedByDescending { it.priorite }
        FiltreEtat.EN_RETARD -> tasks.filter { isEnRetard(it) }.sortedByDescending { it.priorite }
        FiltreEtat.REALISEE  -> tasks.filter { it.isCompleted }.sortedByDescending { it.priorite }
    }

    // Référence aux vues Android custom
    val wateringCanViewRef = remember { mutableStateOf<WateringCanView?>(null) }
    val waterFlowViewRef   = remember { mutableStateOf<WaterFlowView?>(null) }

    // Position de l'arrosoir (centre, en px fenêtre)
    val canX = remember { mutableStateOf(0f) }
    val canY = remember { mutableStateOf(0f) }

    // Niveau d'eau = pts du jour / 50 (se remplit par tranche de 50 pts)
    val pointsDansTranche = dailyPoints % 50
    val waterTarget = (pointsDansTranche * 100) / 50  // 0→100 dans la tranche courante
    val arrosoirsRemplis = dailyPoints / 50

    // Déclencher le bonus quand l'arrosoir est rempli (changement de tranche)
    LaunchedEffect(arrosoirsRemplis) {
        if (arrosoirsRemplis > 0) onWateringCanFull()
    }

    // Message explicite selon progression du jour et nombre de seaux remplis
    val messageGestionnaire = when {
        arrosoirsRemplis == 0 && dailyPoints == 0 -> "Objectif du jour : remplir ton premier seau !"
        arrosoirsRemplis == 0 && pointsDansTranche < 20 -> "Le premier seau se remplit... continue d'arroser 🌱"
        arrosoirsRemplis == 0 && pointsDansTranche < 40 -> "Beau départ ! Ton premier seau prend forme"
        arrosoirsRemplis == 0 -> "Plus beaucoup avant le 1er seau et le bonus multiplicateur +0,1"

        arrosoirsRemplis == 1 && pointsDansTranche == 0 -> "1 seau rempli ! Ton bonus multiplicateur cumulé passe à +0,1"
        arrosoirsRemplis == 1 && pointsDansTranche < 25 -> "Deuxième seau en vue... garde le rythme 💧"
        arrosoirsRemplis == 1 -> "Encore un effort pour décrocher un bonus multiplicateur +0,2"

        arrosoirsRemplis == 2 && pointsDansTranche == 0 -> "2 seaux remplis ! Le bonus multiplicateur monte à +0,2"
        arrosoirsRemplis == 2 && pointsDansTranche < 25 -> "Le troisième seau se prépare tranquillement"
        arrosoirsRemplis == 2 -> "Plus que quelques gouttes pour atteindre un bonus multiplicateur +0,3"

        pointsDansTranche == 0 -> "$arrosoirsRemplis seaux remplis ! Le bonus multiplicateur grimpe à +${String.format(Locale.US, "%.1f", arrosoirsRemplis * 0.1)}"
        pointsDansTranche < 25 -> "$arrosoirsRemplis seaux sécurisés, le prochain bonus multiplicateur arrive"
        else -> "Encore une petite pis'tâche pour passer à un bonus multiplicateur +${String.format(Locale.US, "%.1f", (arrosoirsRemplis + 1) * 0.1)}"
    }

    val sousTitre = if (arrosoirsRemplis > 0)
        "$arrosoirsRemplis seau${if (arrosoirsRemplis > 1) "x" else ""} rempli${if (arrosoirsRemplis > 1) "s" else ""} · bonus multiplicateur cumulé +${String.format(Locale.US, "%.1f", arrosoirsRemplis * 0.1)} · $pointsDansTranche/50 pts vers le suivant"
    else
        "$pointsDansTranche / 50 pts pour remplir le premier seau · chaque seau ajoute +0,1 au bonus multiplicateur"

    // Variables d'état pour la suppression de tâche
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .then(modifier)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            // ── En-tête ──────────────────────────────────────────────
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
                            contentDescription = "Points",
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(BleuTurquoise)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$totalPoints",
                            color = BleuTurquoise,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Image(
                        painter = painterResource(id = R.drawable.pistache),
                        contentDescription = "Logo",
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "Pistask",
                        color = VertPistacheFoncee,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 0.dp)
                    )
                }
            }

            // ── Top card avec arrosoir ───────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = VertKaki,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Arrosoir à gauche
                    AndroidView(
                        factory = { ctx ->
                            WateringCanView(ctx).also { view ->
                                wateringCanViewRef.value = view
                                view.waterLevel = waterTarget
                            }
                        },
                        update = { view ->
                            wateringCanViewRef.value = view
                            view.animateTo(waterTarget)
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                val size = coords.size
                                canX.value = pos.x + size.width / 2f
                                canY.value = pos.y + size.height / 2f
                            }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Texte résumé
                    Column {
                        Text(
                            text = "CHASSE AUX SEAUX",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = messageGestionnaire,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sousTitre,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Titre "Vos tâches"
            Text(text = "Vos pis'tâches", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

        // ── CHIPS DE FILTRE ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(FiltreEtat.TOUTES,    "Toutes",     VertPistacheClair),
                Triple(FiltreEtat.A_FAIRE,   "À faire",    VertPistacheFoncee),
                Triple(FiltreEtat.EN_RETARD, "En retard",  Orange),
                Triple(FiltreEtat.REALISEE,  "Réalisée",   BleuTurquoise)
            ).forEach { (filtre, label, couleur) ->
                val selected = filtreSelectionne == filtre
                FilterChip(
                    selected = selected,
                    onClick = { filtreSelectionne = filtre },
                    label = { Text(text = label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = couleur,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = couleur.copy(alpha = 0.4f),
                        selectedBorderColor = couleur
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
                    contentPadding = PaddingValues(bottom = 180.dp)
                ) {
                    items(filteredTasks) { task ->
                        val checkboxCenter = remember { mutableStateOf(Pair(0f, 0f)) }
                        Box(
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                // La checkbox est à ~32dp du bord gauche de la carte
                                checkboxCenter.value = Pair(
                                    pos.x + 80f,   // centre approximatif de la checkbox (48dp/2 + 16dp padding)
                                    pos.y + coords.size.height / 2f
                                )
                            }
                        ) {
                            TaskCard(
                                task = task,
                                onCheckClick = {
                                    if (!task.isCompleted) {
                                        waterFlowViewRef.value?.startFlow(
                                            startX = checkboxCenter.value.first,
                                            startY = checkboxCenter.value.second,
                                            endX   = canX.value,
                                            endY   = canY.value
                                        )
                                    }
                                    onTaskCheck(task)
                                },
                                onEditClick = { onEditRequest(task) },
                                onDeleteClick = {
                                    taskToDelete = task
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── OVERLAY WaterFlow (par-dessus tout) ───────────────────────
        AndroidView(
            factory = { ctx ->
                WaterFlowView(ctx).also { waterFlowViewRef.value = it }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Confirmation de suppression
    DeleteTaskDialog(
        show = showDeleteDialog,
        task = taskToDelete,
        onConfirm = {
            onTaskDelete(taskToDelete!!)
            showDeleteDialog = false
            taskToDelete = null
        },
        onDismiss = {
            showDeleteDialog = false
            taskToDelete = null
        }
    )
}
