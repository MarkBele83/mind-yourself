package de.stroebele.mindyourself

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import de.stroebele.mindyourself.wear.sync.WearStartupRestoreUseCase
import de.stroebele.mindyourself.worker.ReminderEvaluationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MindYourselfWearApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var wearStartupRestoreUseCase: WearStartupRestoreUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        ReminderEvaluationWorker.schedule(WorkManager.getInstance(this))
        Log.d(TAG, "ReminderEvaluationWorker ensured on process start")

        // Restore reminder configs from DataLayer if local DB is empty
        // (e.g. after a fresh install / uninstall+reinstall).
        appScope.launch {
            wearStartupRestoreUseCase()
        }
    }

    companion object {
        private const val TAG = "MindYourselfWearApp"
    }
}
