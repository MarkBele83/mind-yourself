package de.stroebele.mindyourself.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.stroebele.mindyourself.worker.RegisterPassiveMonitoringWorker
import de.stroebele.mindyourself.worker.ReminderEvaluationWorker

/**
 * Runs after device reboot.
 * - Re-registers PassiveListenerService (passive registrations do not survive reboots).
 * - Re-confirms the periodic ReminderEvaluationWorker (WorkManager persists it across reboots,
 *   but re-enqueuing with KEEP is a safe no-op and guards against a wiped WorkManager DB).
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.i(TAG, "Boot completed — re-registering passive monitoring and reminder worker")

        val workManager = WorkManager.getInstance(context)

        // Re-register Health Services passive listener (mandatory after every reboot)
        workManager.enqueue(OneTimeWorkRequestBuilder<RegisterPassiveMonitoringWorker>().build())

        // Ensure periodic reminder evaluation is scheduled
        ReminderEvaluationWorker.schedule(workManager)
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
