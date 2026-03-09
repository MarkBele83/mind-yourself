package de.stroebele.mindyourself.data.repository

import de.stroebele.mindyourself.data.db.dao.HealthCacheDao
import de.stroebele.mindyourself.data.db.entity.HealthCacheEntity
import de.stroebele.mindyourself.data.db.entity.HeartRateCacheEntity
import de.stroebele.mindyourself.data.db.mapper.toDomain
import de.stroebele.mindyourself.domain.model.ActivityState
import de.stroebele.mindyourself.domain.model.HealthCache
import de.stroebele.mindyourself.domain.model.HeartRateEntry
import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class HealthCacheRepositoryImpl @Inject constructor(
    private val dao: HealthCacheDao,
) : HealthCacheRepository {

    override fun observeTodaySteps(): Flow<Long> =
        dao.observeTodaySteps(startOfTodayMs())

    override suspend fun getStepsBetween(from: Instant, to: Instant): Long =
        dao.getStepsBetween(from.toEpochMilli(), to.toEpochMilli())

    override suspend fun continuousDurationInState(state: ActivityState, until: Instant): Long {
        val untilMs = until.toEpochMilli()
        val lastChangeMs = dao.lastStateChangeAwayFrom(state.name, untilMs) ?: return 0L
        return untilMs - lastChangeMs
    }

    override suspend fun saveHealthSnapshot(entry: HealthCache) {
        dao.insertHealthSnapshot(
            HealthCacheEntity(
                steps = entry.steps,
                activityState = entry.activityState.name,
                timestampEpochMs = entry.timestamp.toEpochMilli(),
            )
        )
    }

    override suspend fun saveHeartRate(entry: HeartRateEntry) {
        dao.insertHeartRate(
            HeartRateCacheEntity(
                bpm = entry.bpm,
                timestampEpochMs = entry.timestamp.toEpochMilli(),
                synced = entry.synced,
            )
        )
    }

    override suspend fun getUnsyncedHeartRates(): List<HeartRateEntry> =
        dao.getUnsyncedHeartRates().map { it.toDomain() }

    override suspend fun markHeartRatesSynced(ids: List<Long>) =
        dao.markHeartRatesSynced(ids)

    override suspend fun deleteOlderThan(before: Instant) {
        val beforeMs = before.toEpochMilli()
        dao.deleteHealthOlderThan(beforeMs)
        dao.deleteHeartRateOlderThan(beforeMs)
    }

    private fun startOfTodayMs(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
