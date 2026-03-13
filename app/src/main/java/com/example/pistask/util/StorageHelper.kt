package com.example.pistask.util

import android.content.Context
import android.content.SharedPreferences
import com.example.pistask.presentation.components.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StorageHelper {
    private const val PREFS_NAME = "pistask_prefs"
    private const val TASKS_KEY = "tasks_list"
    private const val TOTAL_POINTS_KEY = "total_points"
    private const val DAILY_POINTS_KEY = "daily_points"
    private const val LAST_RESET_DAY_KEY = "last_reset_day"
    private const val BONUS_MULTIPLIER_KEY = "bonus_multiplier"
    private const val GROWTH_KEY = "plant_growth"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveTasks(context: Context, tasks: List<Task>) {
        val json = gson.toJson(tasks)
        getPrefs(context).edit().putString(TASKS_KEY, json).apply()
    }

    fun loadTasks(context: Context): List<Task> {
        val json = getPrefs(context).getString(TASKS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Task>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveTotalPoints(context: Context, points: Int) {
        getPrefs(context).edit().putInt(TOTAL_POINTS_KEY, points).apply()
    }

    fun loadTotalPoints(context: Context): Int =
        getPrefs(context).getInt(TOTAL_POINTS_KEY, 0)

    fun saveDailyPoints(context: Context, points: Int) {
        getPrefs(context).edit().putInt(DAILY_POINTS_KEY, points).apply()
    }

    fun loadDailyPoints(context: Context): Int =
        getPrefs(context).getInt(DAILY_POINTS_KEY, 0)

    fun saveLastResetDay(context: Context, day: String) {
        getPrefs(context).edit().putString(LAST_RESET_DAY_KEY, day).apply()
    }

    fun loadLastResetDay(context: Context): String =
        getPrefs(context).getString(LAST_RESET_DAY_KEY, "") ?: ""

    fun saveBonusMultiplier(context: Context, bonus: Double) {
        getPrefs(context).edit().putFloat(BONUS_MULTIPLIER_KEY, bonus.toFloat()).apply()
    }

    fun loadBonusMultiplier(context: Context): Double =
        getPrefs(context).getFloat(BONUS_MULTIPLIER_KEY, 1.0f).toDouble()
    fun saveGrowth(context: Context, growth: Float) {
        getPrefs(context).edit().putFloat(GROWTH_KEY, growth).apply()
    }

    fun loadGrowth(context: Context): Float {
        return getPrefs(context).getFloat(GROWTH_KEY, 0f)
    }
}
