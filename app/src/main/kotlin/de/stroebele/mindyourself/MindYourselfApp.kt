package de.stroebele.mindyourself

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import de.stroebele.mindyourself.worker.HcSyncWorker
import javax.inject.Inject

@HiltAndroidApp
class MindYourselfApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        HcSyncWorker.schedule(WorkManager.getInstance(this))
    }
}
