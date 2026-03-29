package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.HydrationPortionSize
import kotlinx.coroutines.flow.Flow

interface HydrationPortionSizeRepository {
    fun observeAll(): Flow<List<HydrationPortionSize>>
    suspend fun getAll(): List<HydrationPortionSize>
    suspend fun save(size: HydrationPortionSize)
    suspend fun delete(id: Long)
}
