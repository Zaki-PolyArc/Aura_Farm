package com.example.aurafarm2.features.expenses

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.aurafarm2.REMINDER_CHANNEL_ID
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class DailyRecurringWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val recurringEntries = recurringEntriesFlow(context).first()
        val today = LocalDate.now().toEpochDay()
        
        recurringEntries.filter { it.enabled }.forEach { entry ->
            val daysUntil = entry.nextDueEpochDay - today
            if (daysUntil in 0L..1L) {
                if (canPostNotifications()) {
                    val title = if (daysUntil == 0L) "Due Today: ${entry.name}" else "Due Tomorrow: ${entry.name}"
                    val text = "Amount: ${entry.amount} (${entry.kind})"
                    
                    val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(entry.id.hashCode(), notification)
                }
            }
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

fun scheduleDailyRecurringWorker(context: Context) {
    val now = LocalDateTime.now()
    var target = now.withHour(9).withMinute(0).withSecond(0).withNano(0)
    if (!target.isAfter(now)) {
        target = target.plusDays(1)
    }
    val delayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(1)

    val request = PeriodicWorkRequestBuilder<DailyRecurringWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "aura_daily_recurring",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
