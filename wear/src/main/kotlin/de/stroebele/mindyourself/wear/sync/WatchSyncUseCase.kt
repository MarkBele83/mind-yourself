package de.stroebele.mindyourself.wear.sync

import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import de.stroebele.mindyourself.sync.WearDataClient
import de.stroebele.mindyourself.sync.model.HeartRateDto
import de.stroebele.mindyourself.sync.model.HydrationLogDto
import de.stroebele.mindyourself.sync.model.SupplementLogDto
import javax.inject.Inject

/**
 * Orchestrates the manual Watch → Phone sync triggered by the user.
 * Pushes all unsynced logs via Data Layer API, then marks them as synced in Room.
 */
class WatchSyncUseCase @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val supplementRepository: SupplementRepository,
    private val healthCacheRepository: HealthCacheRepository,
    private val wearDataClient: WearDataClient,
) {
    suspend operator fun invoke() {
        val hydrationLogs = hydrationRepository.getUnsynced()
        val supplementLogs = supplementRepository.getUnsynced()
        val heartRateLogs = healthCacheRepository.getUnsyncedHeartRates()

        if (hydrationLogs.isNotEmpty()) {
            wearDataClient.pushHydrationLogs(hydrationLogs.map {
                HydrationLogDto(id = it.id, amountMl = it.amountMl, timestampEpochMs = it.timestamp.toEpochMilli())
            })
            hydrationRepository.markSynced(hydrationLogs.map { it.id })
        }

        if (supplementLogs.isNotEmpty()) {
            wearDataClient.pushSupplementLogs(supplementLogs.map {
                SupplementLogDto(id = it.id, supplementName = it.supplementName, takenAtEpochMs = it.takenAt.toEpochMilli())
            })
            supplementRepository.markSynced(supplementLogs.map { it.id })
        }

        if (heartRateLogs.isNotEmpty()) {
            wearDataClient.pushHeartRateLogs(heartRateLogs.map {
                HeartRateDto(id = it.id, bpm = it.bpm, timestampEpochMs = it.timestamp.toEpochMilli())
            })
            healthCacheRepository.markHeartRatesSynced(heartRateLogs.map { it.id })
        }
    }
}
