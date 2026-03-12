package com.example.pistask.util

import android.content.Context
import android.content.SharedPreferences
import com.example.pistask.presentation.components.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StorageHelper {
    private const val PREFS_NAME = "pistask_prefs"
    private const val TASKS_KEY = "tasks_list"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTasks(context: Context, tasks: List<Task>) {
        val json = gson.toJson(tasks)
        getPrefs(context).edit().putString(TASKS_KEY, json).apply()
    }

    fun loadTasks(context: Context): List<Task> {
        val json = getPrefs(context).getString(TASKS_KEY, null)
        return if (json == null) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(json, type)
        }
    }
}
