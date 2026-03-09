package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.ActivityState
import de.stroebele.mindyourself.domain.model.HealthCache
import de.stroebele.mindyourself.domain.model.HeartRateEntry
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface HealthCacheRepository {
    /** Observable today's step total — updates whenever new data arrives from PassiveListenerService */
    fun observeTodaySteps(): Flow<Long>
    /** Steps recorded within a time window, used by MovementReminder evaluation */
    suspend fun getStepsBetween(from: Instant, to: Instant): Long
    /** How long the device has been continuously in [state] up to [until] */
    suspend fun continuousDurationInState(state: ActivityState, until: Instant): Long
    suspend fun saveHealthSnapshot(entry: HealthCache)
    suspend fun saveHeartRate(entry: HeartRateEntry)
    suspend fun getUnsyncedHeartRates(): List<HeartRateEntry>
    suspend fun markHeartRatesSynced(ids: List<Long>)
    suspend fun deleteOlderThan(before: Instant)
}
