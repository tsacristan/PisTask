package com.example.pistask

// Priorité
enum class Priorite { BASSE, MOYENNE, HAUTE }

// Récurrence (FR)
enum class Recurrence { QUOTIDIEN, HEBDOMADAIRE, MENSUEL, TRIMESTRIEL, SEMESTRIEL, ANNUEL }

// Modèle de données Task
data class Task(
    val id: Int,
    val title: String,
    val subtitle: String,
    val recurrence: Recurrence,
    val date: String,
    val priorite: Priorite,
    val points: Int,
    val isCompleted: Boolean = false
)
