package de.stroebele.mindyourself.data.repository

import de.stroebele.mindyourself.data.db.dao.SupplementDao
import de.stroebele.mindyourself.data.db.entity.SupplementLogEntity
import de.stroebele.mindyourself.data.db.mapper.toDomain
import de.stroebele.mindyourself.domain.model.SupplementLog
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class SupplementRepositoryImpl @Inject constructor(
    private val dao: SupplementDao,
) : SupplementRepository {

    override fun observeToday(): Flow<List<SupplementLog>> =
        dao.observeToday(startOfTodayMs()).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getUnsynced(): List<SupplementLog> =
        dao.getUnsynced().map { it.toDomain() }

    override suspend fun log(supplementName: String): Long =
        dao.insert(SupplementLogEntity(supplementName = supplementName, takenAtEpochMs = Instant.now().toEpochMilli()))

    override suspend fun markSynced(ids: List<Long>) =
        dao.markSynced(ids)

    override suspend fun deleteOlderThan(before: Instant) =
        dao.deleteOlderThan(before.toEpochMilli())

    override suspend fun saveAll(logs: List<SupplementLog>) =
        dao.insertAll(logs.map { SupplementLogEntity(id = it.id, supplementName = it.supplementName, takenAtEpochMs = it.takenAt.toEpochMilli(), synced = it.synced) })

    private fun startOfTodayMs(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
