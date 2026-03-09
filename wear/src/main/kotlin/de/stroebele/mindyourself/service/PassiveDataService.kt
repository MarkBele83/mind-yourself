package de.stroebele.mindyourself.service

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveMonitoringUpdate
import androidx.health.services.client.data.UserActivityInfo
import androidx.health.services.client.data.UserActivityState
import dagger.hilt.android.AndroidEntryPoint
import de.stroebele.mindyourself.domain.model.ActivityState
import de.stroebele.mindyourself.domain.model.HealthCache
import de.stroebele.mindyourself.domain.model.HeartRateEntry
import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Background service that receives batched sensor data from Health Services.
 * Survives app process death — registered via PassiveMonitoringClient.
 * Re-registered after reboot via [BootCompletedReceiver].
 */
@AndroidEntryPoint
class PassiveDataService : PassiveListenerService() {

    @Inject lateinit var healthCacheRepository: HealthCacheRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        scope.launch {
            handleSteps(dataPoints)
            handleHeartRate(dataPoints)
        }
    }

    override fun onUserActivityInfoReceived(info: UserActivityInfo) {
        scope.launch {
            val state = info.userActivityState.toDomain()
            // Steps delta not available directly from UserActivityInfo — snapshot with 0
            // to record the state transition timestamp for sedentary duration calculation.
            healthCacheRepository.saveHealthSnapshot(
                HealthCache(
                    steps = 0L,
                    activityState = state,
                    timestamp = Instant.now(),
                )
            )
            Log.d(TAG, "Activity state changed → $state")
        }
    }

    private suspend fun handleSteps(dataPoints: DataPointContainer) {
        val stepsPoints = dataPoints.getData(DataType.STEPS)
        if (stepsPoints.isEmpty()) return

        val totalSteps = stepsPoints.sumOf { it.value }
        healthCacheRepository.saveHealthSnapshot(
            HealthCache(
                steps = totalSteps,
                activityState = ActivityState.UNKNOWN,
                timestamp = Instant.now(),
            )
        )
        Log.d(TAG, "Steps received: $totalSteps")
    }

    private suspend fun handleHeartRate(dataPoints: DataPointContainer) {
        val hrPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        hrPoints.forEach { point ->
            healthCacheRepository.saveHeartRate(
                HeartRateEntry(
                    bpm = point.value,
                    timestamp = Instant.now(),
                )
            )
        }
        if (hrPoints.isNotEmpty()) {
            Log.d(TAG, "Heart rate samples received: ${hrPoints.size}")
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "PassiveDataService"
    }
}

private fun UserActivityState.toDomain(): ActivityState = when (this) {
    UserActivityState.USER_ACTIVITY_PASSIVE -> ActivityState.PASSIVE
    UserActivityState.USER_ACTIVITY_EXERCISE -> ActivityState.EXERCISE
    UserActivityState.USER_ACTIVITY_ASLEEP -> ActivityState.ASLEEP
    else -> ActivityState.UNKNOWN
}
