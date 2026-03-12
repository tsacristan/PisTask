package com.example.pistask.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pistask.R

object NotificationHelper {
    private const val CHANNEL_ID = "pistask_notifications"
    private const val CHANNEL_NAME = "Notifications PisTask"
    private const val CHANNEL_DESC = "Notifications pour les rappels de tâches"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showUpcomingDeadlineNotification(context: Context, taskTitle: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.leaf) // Assumed existing drawable from previous edits
            .setContentTitle("Attention, petite pistache !")
            .setContentText("Ta tâche \"$taskTitle\" va bientôt être grillée. Dépêche-toi de la récolter !")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskTitle.hashCode(), builder.build())
    }

    fun showLateNotification(context: Context, taskTitle: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.leaf)
            .setContentTitle("Oh non, une pistache perdue !")
            .setContentText("Ta tâche \"$taskTitle\" est en retard. Ne la laisse pas se faner davantage !")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskTitle.hashCode() + 1, builder.build())
    }
}
