package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.HydrationLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface HydrationRepository {
    fun observeToday(): Flow<List<HydrationLog>>
    suspend fun getTodayTotalMl(): Int
    suspend fun getUnsynced(): List<HydrationLog>
    suspend fun log(amountMl: Int): Long
    /** Removes the most recent today-entry with the given amount (undo mis-tap). */
    suspend fun removeLatestOfAmount(amountMl: Int)
    /** Removes the most recent today-entry regardless of amount (global undo). */
    suspend fun removeLatest()
    suspend fun markSynced(ids: List<Long>)
    /** Delete entries older than [before] */
    suspend fun deleteOlderThan(before: Instant)
    /** Persist synced logs received from Watch — used by PhoneSyncService */
    suspend fun saveAll(logs: List<HydrationLog>)
}
