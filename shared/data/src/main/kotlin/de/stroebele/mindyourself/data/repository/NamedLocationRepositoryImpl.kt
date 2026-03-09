package de.stroebele.mindyourself.data.repository

import de.stroebele.mindyourself.data.db.dao.NamedLocationDao
import de.stroebele.mindyourself.data.db.mapper.toDomain
import de.stroebele.mindyourself.data.db.mapper.toEntity
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.domain.repository.NamedLocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NamedLocationRepositoryImpl @Inject constructor(
    private val dao: NamedLocationDao,
) : NamedLocationRepository {

    override fun observeAll(): Flow<List<NamedLocation>> =
        dao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun getAll(): List<NamedLocation> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): NamedLocation? =
        dao.getById(id)?.toDomain()

    override suspend fun save(location: NamedLocation): Long =
        dao.upsert(location.toEntity())

    override suspend fun delete(id: Long) =
        dao.deleteById(id)

    override suspend fun isNameTaken(name: String, excludeId: Long): Boolean =
        dao.countByName(name.trim(), excludeId) > 0
}
