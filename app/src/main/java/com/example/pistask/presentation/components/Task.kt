package com.example.pistask.presentation.components

// Priorité
enum class Priorite { BASSE, MOYENNE, HAUTE }

// Récurrence (FR)
enum class Recurrence { UNIQUE, QUOTIDIEN, HEBDOMADAIRE, MENSUEL, TRIMESTRIEL, SEMESTRIEL, ANNUEL }

// Modèle de données Task
data class Task(
    val id: Int,
    val title: String,
    val subtitle: String,
    val recurrence: Recurrence,
    val date: String,           // date d'échéance affichée
    val nextDate: String = "",  // prochaine occurrence (après validation)
    val priorite: Priorite,
    val points: Int,
    val isCompleted: Boolean = false
)