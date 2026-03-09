package de.stroebele.mindyourself.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.stroebele.mindyourself.service.PassiveMonitoringManager

/**
 * One-shot worker that re-registers passive monitoring after reboot.
 * WorkManager ensures it runs even if the app is not in the foreground.
 */
@HiltWorker
class RegisterPassiveMonitoringWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val passiveMonitoringManager: PassiveMonitoringManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        passiveMonitoringManager.register()
        return Result.success()
    }
}
