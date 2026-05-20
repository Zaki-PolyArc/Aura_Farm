package com.example.aurafarm2.features.expenses

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aurafarm2.REMINDER_CHANNEL_ID
import com.example.aurafarm2.R
import kotlinx.coroutines.flow.first

class ReminderNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString("id").orEmpty()
        val title = inputData.getString("title").orEmpty().ifBlank { "Aura Farm reminder" }
        val type = inputData.getString("type").orEmpty()
        val note = inputData.getString("note").orEmpty()

        if (canPostNotifications()) {
            val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(note.ifBlank { type })
                .setStyle(NotificationCompat.BigTextStyle().bigText(note.ifBlank { type }))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(reminderId.hashCode(), notification)
        }

        val reminder = remindersFlow(context).first().firstOrNull { it.id == reminderId }
        if (reminder != null && reminder.enabled && reminder.repeat != "Once") {
            scheduleReminder(context, reminder)
        }

        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
