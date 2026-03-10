package com.example.pistask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.graphics.toColorInt
import com.example.pistask.presentation.jardin.JardinScene
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pistask.presentation.components.Priorite
import com.example.pistask.presentation.components.Recurrence
import com.example.pistask.presentation.components.Task
import com.example.pistask.presentation.home.HomeScene
import com.example.pistask.presentation.theme.PisTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // edge-to-edge config and set system navigation bar color to match the app nav bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = "#1C1C14".toColorInt() // même couleur que la nav bar
        window.statusBarColor = "#1C1C14".toColorInt() // barre système du haut = même couleur
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            PisTaskTheme {
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
                                    tasks = tasks.map {
                                        if (it.id == checkedTask.id) it.copy(isCompleted = !it.isCompleted) else it
                                    }.sortedBy { it.isCompleted }
                                },
                                onTaskEdit = { updatedTask ->
                                    tasks = tasks.map {
                                        if (it.id == updatedTask.id) updatedTask else it
                                    }.sortedBy { it.isCompleted }
                                    showEditDialog = false
                                },
                                onEditRequest = { task ->
                                    taskToEdit = task
                                    showEditDialog = true
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
                    onSave = { title, subtitle, date, recurrence, priorite ->
                        try {
                            val newTask = Task(
                                id = tasks.size + 1,
                                title = title,
                                subtitle = subtitle,
                                recurrence = Recurrence.valueOf(recurrence.uppercase()),
                                date = date,
                                priorite = Priorite.valueOf(priorite.uppercase()),
                                points = 10
                            )
                            tasks = tasks + newTask
                        } catch (e: Exception) {
                            // Fallback or log error for invalid enum values
                            val newTask = Task(
                                id = tasks.size + 1,
                                title = title,
                                subtitle = subtitle,
                                recurrence = Recurrence.QUOTIDIEN,
                                date = date,
                                priorite = Priorite.MOYENNE,
                                points = 10
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

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    PisTaskTheme {
        HomeScene(tasks = listOf(
            Task(
                1,
                "Exemple 1",
                "Description 1",
                Recurrence.QUOTIDIEN,
                "2026-03-09",
                Priorite.HAUTE,
                10
            ),
            Task(
                2,
                "Exemple 2",
                "Description 2",
                Recurrence.HEBDOMADAIRE,
                "2026-03-10",
                Priorite.BASSE,
                10
            )
        ), onTaskCheck = {})
    }
}
