package com.example.aurafarm2.features.expenses

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TransactionNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = listOf(
            extras.getCharSequence("android.text")?.toString().orEmpty(),
            extras.getCharSequence("android.bigText")?.toString().orEmpty()
        ).filter { it.isNotBlank() }.joinToString(" ")
        if (title.isBlank() && text.isBlank()) return

        scope.launch {
            NotificationTransactionProcessor.process(
                context = applicationContext,
                sourceApp = sbn.packageName,
                title = title,
                text = text,
                postedAt = sbn.postTime
            )
        }
    }
}
