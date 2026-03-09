package de.stroebele.mindyourself.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.stroebele.mindyourself.notification.Action
import de.stroebele.mindyourself.notification.NotificationDispatcher
import de.stroebele.mindyourself.worker.HandleNotificationActionWorker

/**
 * Handles taps on notification action buttons.
 * Delegates to a Worker so the coroutine-based logic runs safely off the main thread.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val actionName = intent.getStringExtra(NotificationDispatcher.EXTRA_ACTION) ?: return
        val notifId = intent.getIntExtra(NotificationDispatcher.EXTRA_NOTIFICATION_ID, -1)
        val supplementName = intent.getStringExtra(NotificationDispatcher.EXTRA_SUPPLEMENT_NAME)

        Log.d(TAG, "Notification action: $actionName (notif=$notifId)")

        val data = Data.Builder()
            .putString(HandleNotificationActionWorker.KEY_ACTION, actionName)
            .putInt(HandleNotificationActionWorker.KEY_NOTIF_ID, notifId)
            .apply { supplementName?.let { putString(HandleNotificationActionWorker.KEY_SUPPLEMENT_NAME, it) } }
            .build()

        val request = OneTimeWorkRequestBuilder<HandleNotificationActionWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        private const val TAG = "NotificationActionReceiver"
    }
}
