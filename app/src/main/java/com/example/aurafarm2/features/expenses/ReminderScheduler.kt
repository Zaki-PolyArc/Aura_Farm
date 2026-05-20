package com.example.aurafarm2.features.expenses

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val WORK_PREFIX = "aura_reminder_"

fun scheduleReminder(context: Context, reminder: Reminder) {
    cancelReminder(context, reminder.id)
    if (!reminder.enabled) return

    val input = Data.Builder()
        .putString("id", reminder.id)
        .putString("title", reminder.title)
        .putString("type", reminder.type)
        .putString("note", reminder.note)
        .build()

    val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
        .setInputData(input)
        .setInitialDelay(nextReminderDelayMinutes(reminder), TimeUnit.MINUTES)
        .addTag(WORK_PREFIX + reminder.id)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        WORK_PREFIX + reminder.id,
        ExistingWorkPolicy.REPLACE,
        request
    )
}

fun cancelReminder(context: Context, reminderId: String) {
    WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + reminderId)
}

private fun nextReminderDelayMinutes(reminder: Reminder): Long {
    val now = LocalDateTime.now()
    var target = now
        .withHour(reminder.hour.coerceIn(0, 23))
        .withMinute(reminder.minute.coerceIn(0, 59))
        .withSecond(0)
        .withNano(0)
    if (!target.isAfter(now)) {
        target = when (reminder.repeat) {
            "Weekly" -> target.plusWeeks(1)
            else -> target.plusDays(1)
        }
    }
    return Duration.between(now, target).toMinutes().coerceAtLeast(1)
}
