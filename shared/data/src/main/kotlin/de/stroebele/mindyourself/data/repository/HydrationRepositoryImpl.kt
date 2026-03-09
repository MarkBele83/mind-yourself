package de.stroebele.mindyourself.data.repository

import de.stroebele.mindyourself.data.db.dao.HydrationDao
import de.stroebele.mindyourself.data.db.entity.HydrationLogEntity
import de.stroebele.mindyourself.data.db.mapper.toDomain
import de.stroebele.mindyourself.domain.model.HydrationLog
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class HydrationRepositoryImpl @Inject constructor(
    private val dao: HydrationDao,
) : HydrationRepository {

    override fun observeToday(): Flow<List<HydrationLog>> =
        dao.observeToday(startOfTodayMs()).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTodayTotalMl(): Int =
        dao.getTodayTotalMl(startOfTodayMs())

    override suspend fun getUnsynced(): List<HydrationLog> =
        dao.getUnsynced().map { it.toDomain() }

    override suspend fun log(amountMl: Int): Long =
        dao.insert(HydrationLogEntity(amountMl = amountMl, timestampEpochMs = Instant.now().toEpochMilli()))

    override suspend fun removeLatestOfAmount(amountMl: Int) =
        dao.removeLatestOfAmount(amountMl, startOfTodayMs())

    override suspend fun removeLatest() =
        dao.removeLatestEntry(startOfTodayMs())

    override suspend fun markSynced(ids: List<Long>) =
        dao.markSynced(ids)

    override suspend fun deleteOlderThan(before: Instant) =
        dao.deleteOlderThan(before.toEpochMilli())

    override suspend fun saveAll(logs: List<HydrationLog>) =
        dao.insertAll(logs.map { HydrationLogEntity(id = it.id, amountMl = it.amountMl, timestampEpochMs = it.timestamp.toEpochMilli(), synced = it.synced) })

    private fun startOfTodayMs(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
