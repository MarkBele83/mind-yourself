package de.stroebele.mindyourself.service

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers and unregisters the [PassiveDataService] with Health Services.
 * Must be called on app start and after every device reboot.
 */
@Singleton
class PassiveMonitoringManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client: PassiveMonitoringClient by lazy {
        HealthServices.getClient(context).passiveMonitoringClient
    }

    private val config = PassiveListenerConfig.builder()
        .setDataTypes(
            setOf(
                DataType.STEPS,
                DataType.HEART_RATE_BPM,
            )
        )
        .setShouldUserActivityInfoBeRequested(true)
        .build()

    suspend fun register() {
        try {
            client.setPassiveListenerServiceAsync(PassiveDataService::class.java, config).await()
            Log.d(TAG, "PassiveListenerService registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register PassiveListenerService", e)
        }
    }

    suspend fun unregister() {
        try {
            client.clearPassiveListenerServiceAsync().await()
            Log.d(TAG, "PassiveListenerService unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister PassiveListenerService", e)
        }
    }

    companion object {
        private const val TAG = "PassiveMonitoringManager"
    }
}
