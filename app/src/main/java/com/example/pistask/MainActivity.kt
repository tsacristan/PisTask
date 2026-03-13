package com.example.pistask

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.graphics.toColorInt
import com.example.pistask.presentation.jardin.JardinScene
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pistask.presentation.components.Priorite
import com.example.pistask.presentation.components.Recurrence
import com.example.pistask.presentation.components.Task
import com.example.pistask.presentation.components.prochaineDateRecurrence
import com.example.pistask.presentation.components.recurrenceGenereProchaine
import com.example.pistask.presentation.home.HomeScene
import com.example.pistask.presentation.theme.PisTaskTheme
import com.example.pistask.util.ImageHelper
import com.example.pistask.util.NotificationHelper
import com.example.pistask.util.StorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Points de base par priorité
fun pointsBase(priorite: Priorite): Int = when (priorite) {
    Priorite.BASSE  -> 5
    Priorite.MOYENNE -> 10
    Priorite.HAUTE  -> 20
}

// Multiplicateur par récurrence (plus rare = plus de points)
fun multiplicateurRecurrence(recurrence: Recurrence): Double = when (recurrence) {
    Recurrence.UNIQUE       -> 2.0
    Recurrence.QUOTIDIEN    -> 1.0
    Recurrence.HEBDOMADAIRE -> 1.5
    Recurrence.MENSUEL      -> 2.0
    Recurrence.TRIMESTRIEL  -> 2.5
    Recurrence.SEMESTRIEL   -> 3.0
    Recurrence.ANNUEL       -> 4.0
}

fun pointsPourTache(task: Task): Int =
    (pointsBase(task.priorite) * multiplicateurRecurrence(task.recurrence)).toInt()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        // Configuration plein écran et couleur de la barre de navigation système pour correspondre à l'app
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = "#1C1C14".toColorInt()
        window.statusBarColor = "#1C1C14".toColorInt()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            PisTaskTheme {
                val context = LocalContext.current
                
                // Demande de permission pour les notifications (Android 13+)
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        // Gérer le résultat si nécessaire
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Utilise un Scaffold racine : on met la nav bar dans bottomBar pour éviter les doublons
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val items: List<com.example.pistask.presentation.navigation.Screen> = listOf(
                    com.example.pistask.presentation.navigation.Screen.Tache,
                    com.example.pistask.presentation.navigation.Screen.Jardin
                )

                var showAddDialog by remember { mutableStateOf(false) }
                var tasks by remember { mutableStateOf(StorageHelper.loadTasks(context)) }
                var showEditDialog by remember { mutableStateOf(false) }
                var taskToEdit by remember { mutableStateOf<Task?>(null) }
                var totalPoints by remember { mutableIntStateOf(StorageHelper.loadTotalPoints(context)) }
                var dailyPoints by remember { mutableIntStateOf(StorageHelper.loadDailyPoints(context)) }
                var bonusMultiplier by remember { mutableStateOf(StorageHelper.loadBonusMultiplier(context)) }
                // Reset des points quotidiens à chaque nouveau jour
                var lastResetDay by remember { mutableStateOf(StorageHelper.loadLastResetDay(context).ifEmpty {
                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                }) }

                LaunchedEffect(Unit) {
                    val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                    if (today != lastResetDay) {
                        dailyPoints = 0
                        bonusMultiplier = 1.0
                        lastResetDay = today
                        StorageHelper.saveDailyPoints(context, 0)
                        StorageHelper.saveBonusMultiplier(context, 1.0)
                        StorageHelper.saveLastResetDay(context, today)
                    }
                }

                // Sauvegarde des points dès qu'ils changent
                LaunchedEffect(totalPoints) { StorageHelper.saveTotalPoints(context, totalPoints) }
                LaunchedEffect(dailyPoints) { StorageHelper.saveDailyPoints(context, dailyPoints) }
                LaunchedEffect(bonusMultiplier) { StorageHelper.saveBonusMultiplier(context, bonusMultiplier) }
                LaunchedEffect(lastResetDay) { StorageHelper.saveLastResetDay(context, lastResetDay) }

                // Save tasks whenever they change
                LaunchedEffect(tasks) {
                    StorageHelper.saveTasks(context, tasks)
                    
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val today = Date()
                    val todayStr = sdf.format(today)

                    tasks.forEach { task ->
                        if (!task.isCompleted) {
                            try {
                                val taskDate = sdf.parse(task.date)
                                if (taskDate != null) {
                                    val cal = java.util.Calendar.getInstance()
                                    cal.time = taskDate
                                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                    cal.set(java.util.Calendar.MINUTE, 59)
                                    cal.set(java.util.Calendar.SECOND, 59)
                                    
                                    if (cal.time.before(today)) {
                                        NotificationHelper.showLateNotification(context, task.title)
                                    } else if (task.date == todayStr) {
                                        NotificationHelper.showUpcomingDeadlineNotification(context, task.title)
                                    }
                                }
                            } catch (e: Exception) { }
                        }
                    }
                }

                // Sauvegarder les points dès qu'ils changent
                LaunchedEffect(totalPoints) {
                    StorageHelper.savePoints(context, totalPoints)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        com.example.pistask.presentation.navigation.MyBottomNavigationBar(
                            items = items,
                            currentRoute = currentRoute,
                            onItemClick = { route: String ->
                                if (route != currentRoute) {
                                    navController.navigate(route) { launchSingleTop = true }
                                }
                            },
                            centerButtonSizeDp = 130,
                            centerIconSizeDp = 60,
                            centerVerticalOffsetDp = -24,
                            centerOnClick = { showAddDialog = true }
                        )
                    },
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = com.example.pistask.presentation.navigation.Screen.Tache.route,
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    ) {
                        composable(com.example.pistask.presentation.navigation.Screen.Tache.route) {
                            HomeScene(
                                tasks = tasks,
                                totalPoints = totalPoints,
                                dailyPoints = dailyPoints,
                                onTaskCheck = { checkedTask ->
                                    if (!checkedTask.isCompleted) {
                                        val earned = (pointsPourTache(checkedTask) * bonusMultiplier).toInt()
                                        totalPoints += earned
                                        dailyPoints += earned

                                        val nextDate = if (recurrenceGenereProchaine(checkedTask.recurrence))
                                            prochaineDateRecurrence(checkedTask.date, checkedTask.recurrence)
                                        else ""

                                        tasks = tasks.map { task ->
                                            if (task.id == checkedTask.id) task.copy(
                                                isCompleted = true,
                                                nextDate = nextDate
                                            ) else task
                                        }.sortedBy { it.isCompleted }
                                    } else {
                                        val lost = (pointsPourTache(checkedTask) * bonusMultiplier).toInt()
                                        totalPoints = maxOf(0, totalPoints - lost)
                                        dailyPoints = maxOf(0, dailyPoints - lost)

                                        tasks = tasks.map { task ->
                                            if (task.id == checkedTask.id) task.copy(
                                                isCompleted = false,
                                                nextDate = ""
                                            ) else task
                                        }.sortedBy { it.isCompleted }
                                    }
                                },
                                onWateringCanFull = {
                                    // Bonus ×1.5 actif jusqu'à la fin de la journée
                                    bonusMultiplier = 1.5
                                },
                                onEditRequest = { task ->
                                    taskToEdit = task
                                    showEditDialog = true
                                },
                                onAutoReset = { tasksToReset ->
                                    tasks = tasks.map { task ->
                                        if (tasksToReset.any { it.id == task.id })
                                            task.copy(
                                                isCompleted = false,
                                                date = if (task.nextDate.isNotEmpty()) task.nextDate else task.date,
                                                nextDate = ""
                                            )
                                        else task
                                    }.sortedBy { it.isCompleted }
                                },
                                onTaskDelete = { task ->
                                    task.imageUri?.let { ImageHelper.deleteImageFromInternalStorage(it) }
                                    tasks = tasks.filter { it.id != task.id }
                                }
                            )
                        }
                        composable(com.example.pistask.presentation.navigation.Screen.Jardin.route) {
                            JardinScene(
                                initialPoints = totalPoints,
                                onPointsChanged = { newPoints ->
                                    totalPoints = newPoints
                                }
                            )
                        }
                    }
                }

                com.example.pistask.presentation.add.AjouterTacheDialog(
                    show = showAddDialog,
                    onDismiss = { showAddDialog = false },
                    onSave = { title, subtitle, date, recurrence, priorite, imageUri ->
                        try {
                            val savedImagePath = imageUri?.let { ImageHelper.saveImageToInternalStorage(context, it) }
                            val newTask = Task(
                                id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                                title = title,
                                subtitle = subtitle,
                                recurrence = Recurrence.valueOf(recurrence.uppercase()),
                                date = date,
                                priorite = Priorite.valueOf(priorite.uppercase()),
                                points = 10,
                                imageUri = savedImagePath
                            )
                            tasks = tasks + newTask
                        } catch (e: Exception) {
                            val savedImagePath = imageUri?.let { ImageHelper.saveImageToInternalStorage(context, it) }
                            val newTask = Task(
                                id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                                title = title,
                                subtitle = subtitle,
                                recurrence = Recurrence.UNIQUE,
                                date = date,
                                priorite = Priorite.MOYENNE,
                                points = 10,
                                imageUri = savedImagePath
                            )
                            tasks = tasks + newTask
                        }
                        showAddDialog = false
                    }
                )
                if (showEditDialog && taskToEdit != null) {
                    com.example.pistask.presentation.edit.ModifierTacheDialog(
                        show = showEditDialog,
                        task = taskToEdit!!,
                        onDismiss = { showEditDialog = false },
                        onSave = { updatedTask ->
                            val oldImage = tasks.firstOrNull { it.id == updatedTask.id }?.imageUri
                            val savedImagePath = updatedTask.imageUri?.let {
                                if (!it.startsWith(context.filesDir.absolutePath)) {
                                    // nouvelle image : copier en stockage interne
                                    val path = ImageHelper.saveImageToInternalStorage(context, it)
                                    // supprimer l'ancienne si elle change
                                    if (oldImage != null && oldImage != path) {
                                        ImageHelper.deleteImageFromInternalStorage(oldImage)
                                    }
                                    path
                                } else it // déjà en stockage interne, on garde
                            } ?: run {
                                // image supprimée : nettoyer l'ancienne
                                if (oldImage != null) ImageHelper.deleteImageFromInternalStorage(oldImage)
                                null
                            }
                            tasks = tasks.map {
                                if (it.id == updatedTask.id) updatedTask.copy(imageUri = savedImagePath) else it
                            }.sortedBy { it.isCompleted }
                            showEditDialog = false
                        }
                    )
                }
            }
        }
    }
}
