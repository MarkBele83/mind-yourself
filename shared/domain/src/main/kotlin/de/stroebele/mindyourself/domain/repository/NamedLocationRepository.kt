package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.NamedLocation
import kotlinx.coroutines.flow.Flow

interface NamedLocationRepository {
    fun observeAll(): Flow<List<NamedLocation>>
    suspend fun getAll(): List<NamedLocation>
    suspend fun getById(id: Long): NamedLocation?
    suspend fun save(location: NamedLocation): Long
    suspend fun delete(id: Long)
    suspend fun isNameTaken(name: String, excludeId: Long = 0L): Boolean
}
