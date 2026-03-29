package de.stroebele.mindyourself.receiver

import android.app.NotificationManager
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

        Log.d(TAG, "Notification action: $actionName (notif=$notifId)")

        // Navigation actions are handled here directly — Workers cannot start Activities
        if (actionName == Action.OPEN_HYDRATION_LOG.name) {
            context.getSystemService(NotificationManager::class.java).cancel(notifId)
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                ?.let { context.startActivity(it) }
            return
        }

        val data = Data.Builder()
            .putString(HandleNotificationActionWorker.KEY_ACTION, actionName)
            .putInt(HandleNotificationActionWorker.KEY_NOTIF_ID, notifId)
            .build()

        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<HandleNotificationActionWorker>().setInputData(data).build()
        )
    }

    companion object {
        private const val TAG = "NotificationActionReceiver"
    }
}
