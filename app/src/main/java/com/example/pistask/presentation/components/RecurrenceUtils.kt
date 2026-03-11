package com.example.pistask.presentation.components

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Calcule la prochaine date d'échéance selon la récurrence d'une tâche.
 * Format de date attendu : "dd/MM/yyyy"
 */
fun recurrenceGenereProchaine(rec: Recurrence): Boolean = rec != Recurrence.UNIQUE

/** Revient d'un cycle en arrière (pour annuler un check). */
fun datePrecedente(dateActuelle: String, recurrence: Recurrence): String {
    if (recurrence == Recurrence.UNIQUE) return dateActuelle
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val cal = Calendar.getInstance()
        val parsed = sdf.parse(dateActuelle)
        if (parsed != null) cal.time = parsed
        when (recurrence) {
            Recurrence.UNIQUE       -> { }
            Recurrence.QUOTIDIEN    -> cal.add(Calendar.DAY_OF_YEAR, -1)
            Recurrence.HEBDOMADAIRE -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            Recurrence.MENSUEL      -> cal.add(Calendar.MONTH, -1)
            Recurrence.TRIMESTRIEL  -> cal.add(Calendar.MONTH, -3)
            Recurrence.SEMESTRIEL   -> cal.add(Calendar.MONTH, -6)
            Recurrence.ANNUEL       -> cal.add(Calendar.YEAR, -1)
        }
        sdf.format(cal.time)
    } catch (_: Exception) {
        dateActuelle
    }
}

fun prochaineDateRecurrence(dateActuelle: String, recurrence: Recurrence): String {
    if (recurrence == Recurrence.UNIQUE) return dateActuelle
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val cal = Calendar.getInstance()
        val parsed = sdf.parse(dateActuelle)
        if (parsed != null) cal.time = parsed

        val now = Calendar.getInstance()
        // Avancer cycle par cycle jusqu'à obtenir une date dans le futur
        do {
            when (recurrence) {
                Recurrence.UNIQUE       -> break
                Recurrence.QUOTIDIEN    -> cal.add(Calendar.DAY_OF_YEAR, 1)
                Recurrence.HEBDOMADAIRE -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                Recurrence.MENSUEL      -> cal.add(Calendar.MONTH, 1)
                Recurrence.TRIMESTRIEL  -> cal.add(Calendar.MONTH, 3)
                Recurrence.SEMESTRIEL   -> cal.add(Calendar.MONTH, 6)
                Recurrence.ANNUEL       -> cal.add(Calendar.YEAR, 1)
            }
        } while (!cal.after(now))

        sdf.format(cal.time)
    } catch (_: Exception) {
        dateActuelle
    }
}

/**
 * Calcule la date à partir de laquelle la tâche doit être visible (1 période avant l'échéance).
 * - QUOTIDIEN      : visible le jour de l'échéance (J)
 * - HEBDOMADAIRE   : visible 1 semaine avant
 * - MENSUEL        : visible 1 mois avant
 * - TRIMESTRIEL    : visible 1 trimestre avant
 * - SEMESTRIEL     : visible 1 semestre avant
 * - ANNUEL         : visible 1 an avant
 */
fun dateApparition(dateEcheance: String, recurrence: Recurrence): Date {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val cal = Calendar.getInstance()
        val parsed = sdf.parse(dateEcheance)
        if (parsed != null) cal.time = parsed

        when (recurrence) {
            Recurrence.UNIQUE       -> cal.add(Calendar.DAY_OF_YEAR, 0)
            Recurrence.QUOTIDIEN    -> cal.add(Calendar.DAY_OF_YEAR, 0)
            Recurrence.HEBDOMADAIRE -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            Recurrence.MENSUEL      -> cal.add(Calendar.MONTH, -1)
            Recurrence.TRIMESTRIEL  -> cal.add(Calendar.MONTH, -3)
            Recurrence.SEMESTRIEL   -> cal.add(Calendar.MONTH, -6)
            Recurrence.ANNUEL       -> cal.add(Calendar.YEAR, -1)
        }

        cal.time
    } catch (_: Exception) {
        Date()
    }
}

/**
 * Retourne true si la tâche doit être visible aujourd'hui.
 * La date d'apparition est comparée à la fin de journée (23:59:59) de ce même jour.
 */
fun tacheEstVisible(task: Task): Boolean {
    val apparition = dateApparition(task.date, task.recurrence)
    // On considère visible dès le début du jour d'apparition (00:00:00)
    val cal = Calendar.getInstance()
    cal.time = apparition
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return !cal.time.after(Date())
}
