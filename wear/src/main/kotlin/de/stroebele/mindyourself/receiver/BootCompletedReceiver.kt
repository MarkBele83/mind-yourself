package de.stroebele.mindyourself.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.stroebele.mindyourself.worker.RegisterPassiveMonitoringWorker

/**
 * Re-registers PassiveListenerService after device reboot.
 * Passive registrations do not survive reboots — this is mandatory.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d(TAG, "Boot completed — scheduling passive monitoring re-registration")

        val request = OneTimeWorkRequestBuilder<RegisterPassiveMonitoringWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
