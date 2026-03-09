package de.stroebele.mindyourself.sync

import dagger.hilt.android.AndroidEntryPoint
import de.stroebele.mindyourself.domain.model.HeartRateEntry
import de.stroebele.mindyourself.domain.model.HydrationLog
import de.stroebele.mindyourself.domain.model.SupplementLog
import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import de.stroebele.mindyourself.sync.model.HeartRateDto
import de.stroebele.mindyourself.sync.model.HydrationLogDto
import de.stroebele.mindyourself.sync.model.SupplementLogDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Concrete Phone-side Wearable Listener.
 * Receives log data pushed from the Watch and persists it to the Phone's Room DB.
 */
@AndroidEntryPoint
class PhoneSyncService : PhoneSyncListenerService() {

    @Inject lateinit var hydrationRepository: HydrationRepository
    @Inject lateinit var supplementRepository: SupplementRepository
    @Inject lateinit var healthCacheRepository: HealthCacheRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onHydrationLogsReceived(logs: List<HydrationLogDto>) {
        scope.launch {
            hydrationRepository.saveAll(logs.map { it.toDomain() })
        }
    }

    override fun onSupplementLogsReceived(logs: List<SupplementLogDto>) {
        scope.launch {
            supplementRepository.saveAll(logs.map { it.toDomain() })
        }
    }

    override fun onHeartRateLogsReceived(logs: List<HeartRateDto>) {
        scope.launch {
            logs.forEach { dto ->
                healthCacheRepository.saveHeartRate(dto.toDomain())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun HydrationLogDto.toDomain() = HydrationLog(
        id = id,
        amountMl = amountMl,
        timestamp = Instant.ofEpochMilli(timestampEpochMs),
        synced = true,
    )

    private fun SupplementLogDto.toDomain() = SupplementLog(
        id = id,
        supplementName = supplementName,
        takenAt = Instant.ofEpochMilli(takenAtEpochMs),
        synced = true,
    )

    private fun HeartRateDto.toDomain() = HeartRateEntry(
        id = id,
        bpm = bpm,
        timestamp = Instant.ofEpochMilli(timestampEpochMs),
        synced = true,
    )
}
