package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.SupplementLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface SupplementRepository {
    fun observeToday(): Flow<List<SupplementLog>>
    suspend fun getUnsynced(): List<SupplementLog>
    suspend fun log(supplementName: String): Long
    suspend fun markSynced(ids: List<Long>)
    suspend fun deleteOlderThan(before: Instant)
    /** Persist synced logs received from Watch — used by PhoneSyncService */
    suspend fun saveAll(logs: List<SupplementLog>)
}
