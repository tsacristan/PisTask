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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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
import com.example.pistask.presentation.components.datePrecedente
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.pistask.presentation.home.HomeScene
import com.example.pistask.presentation.theme.PisTaskTheme
import com.example.pistask.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        // edge-to-edge config and set system navigation bar color to match the app nav bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = "#1C1C14".toColorInt() // même couleur que la nav bar
        window.statusBarColor = "#1C1C14".toColorInt() // barre système du haut = même couleur
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            PisTaskTheme {
                val context = LocalContext.current
                
                // Permission request for notifications (Android 13+)
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        // Handle result if needed
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
                var tasks by remember { mutableStateOf(listOf<Task>()) }
                var showEditDialog by remember { mutableStateOf(false) }
                var taskToEdit by remember { mutableStateOf<Task?>(null) }
                val scope = rememberCoroutineScope()

                // Logic to trigger notifications when tasks change
                LaunchedEffect(tasks) {
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        com.example.pistask.presentation.navigation.MyBottomNavigationBar(
                            items = items,
                            currentRoute = currentRoute,
                            onItemClick = { route: String ->
                                if (route != currentRoute) {
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                    }
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
                    // NavHost: routes pour la bottom navigation
                    // Padding top via WindowInsets (status bar), la nav bar est en overlay
                    NavHost(
                        navController = navController,
                        startDestination = com.example.pistask.presentation.navigation.Screen.Tache.route,
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    ) {
                        composable(com.example.pistask.presentation.navigation.Screen.Tache.route) {
                            HomeScene(
                                tasks = tasks,
                                onTaskCheck = { checkedTask ->
                                    if (!checkedTask.isCompleted) {
                                        // Cocher : garder la date d'échéance, stocker la prochaine dans nextDate
                                        val nextDate = if (recurrenceGenereProchaine(checkedTask.recurrence))
                                            prochaineDateRecurrence(checkedTask.date, checkedTask.recurrence)
                                        else ""

                                        tasks = tasks.map { task ->
                                            if (task.id == checkedTask.id) task.copy(
                                                isCompleted = true,
                                                nextDate = nextDate
                                                // date reste inchangée
                                            ) else task
                                        }.sortedBy { it.isCompleted }
                                    } else {
                                        // Décocher : remettre non complétée, effacer nextDate
                                        tasks = tasks.map { task ->
                                            if (task.id == checkedTask.id) task.copy(
                                                isCompleted = false,
                                                nextDate = ""
                                            ) else task
                                        }.sortedBy { it.isCompleted }
                                    }
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
                                    tasks = tasks.filter { it.id != task.id }
                                }
                            )
                        }
                        composable(com.example.pistask.presentation.navigation.Screen.Jardin.route) {
                            // simple placeholder
                            JardinScene()
                        }
                    }
                }

                // Dialog d'ajout s'affiche en popup au-dessus de tout
                com.example.pistask.presentation.add.AjouterTacheDialog(
                    show = showAddDialog,
                    onDismiss = { showAddDialog = false },
                    onSave = { title, subtitle, date, recurrence, priorite, imageUri ->
                        try {
                            val newTask = Task(
                                id = tasks.size + 1,
                                title = title,
                                subtitle = subtitle,
                                recurrence = Recurrence.valueOf(recurrence.uppercase()),
                                date = date,
                                priorite = Priorite.valueOf(priorite.uppercase()),
                                points = 10,
                                imageUri = imageUri
                            )
                            tasks = tasks + newTask
                        } catch (e: Exception) {
                            val newTask = Task(
                                id = tasks.size + 1,
                                title = title,
                                subtitle = subtitle,
                                recurrence = Recurrence.QUOTIDIEN,
                                date = date,
                                priorite = Priorite.MOYENNE,
                                points = 10,
                                imageUri = imageUri
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
                            tasks = tasks.map {
                                if (it.id == updatedTask.id) updatedTask else it
                            }.sortedBy { it.isCompleted }
                            showEditDialog = false
                        }
                    )
                }
            }
        }
    }
}
