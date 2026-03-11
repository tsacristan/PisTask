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
import com.example.pistask.presentation.components.Task
import com.example.pistask.presentation.components.TaskCard
import com.example.pistask.presentation.components.WateringCanView
import com.example.pistask.presentation.components.WaterFlowView
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
    onTaskCheck: (Task) -> Unit,
    onEditRequest: (Task) -> Unit
) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

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
        } catch (e: Exception) { false }
    }

    // Une tâche doit s'afficher si :
    // - elle est complétée (filtre réalisée)
    // - elle est en retard (toujours visible)
    // - sa date d'apparition est atteinte (tacheEstVisible)
    fun doitSafficher(task: Task): Boolean =
        task.isCompleted || isEnRetard(task) || tacheEstVisible(task)

    // Filtrage + tri
    val filteredTasks = when (filtreSelectionne) {
        FiltreEtat.TOUTES    -> tasks.filter { doitSafficher(it) }
                                     .sortedWith(compareBy({ it.isCompleted }, { !isEnRetard(it) }))
        FiltreEtat.A_FAIRE   -> tasks.filter { !it.isCompleted && !isEnRetard(it) && tacheEstVisible(it) }
        FiltreEtat.EN_RETARD -> tasks.filter { isEnRetard(it) }
        FiltreEtat.REALISEE  -> tasks.filter { it.isCompleted }
    }

    // Référence aux vues Android custom
    val wateringCanViewRef = remember { mutableStateOf<WateringCanView?>(null) }
    val waterFlowViewRef   = remember { mutableStateOf<WaterFlowView?>(null) }

    // Position de l'arrosoir (centre, en px fenêtre)
    val canX = remember { mutableStateOf(0f) }
    val canY = remember { mutableStateOf(0f) }

    // Niveau d'eau = ratio complétées / total (0-100)
    val waterTarget = if (totalCount > 0) (completedCount * 100) / totalCount else 0

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
                        Text(text = "GESTIONNAIRE DE TÂCHES", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$completedCount / $totalCount complétés",
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Titre "Vos tâches"
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
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredTasks) { task ->
                        // Capte la position du centre de la checkbox
                        var checkboxCenter = remember { mutableStateOf(Pair(0f, 0f)) }
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
                                onEditClick = { onEditRequest(task) }
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
}
