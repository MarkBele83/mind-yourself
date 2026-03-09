package de.stroebele.mindyourself.data.repository

import androidx.room.withTransaction
import de.stroebele.mindyourself.data.db.AppDatabase
import de.stroebele.mindyourself.data.db.dao.ReminderConfigDao
import de.stroebele.mindyourself.data.db.mapper.toDomain
import de.stroebele.mindyourself.data.db.mapper.toEntity
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderConfigRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val dao: ReminderConfigDao,
) : ReminderConfigRepository {

    override fun observeAll(): Flow<List<ReminderConfig>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAll(): List<ReminderConfig> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): ReminderConfig? =
        dao.getById(id)?.toDomain()

    override suspend fun save(config: ReminderConfig): Long =
        dao.upsert(config.toEntity())

    override suspend fun delete(id: Long) =
        dao.deleteById(id)

    override suspend fun replaceAll(configs: List<ReminderConfig>) =
        db.withTransaction {
            dao.deleteAll()
            dao.upsertAll(configs.map { it.toEntity() })
        }
}
