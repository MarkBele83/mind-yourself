package de.stroebele.mindyourself.data.repository

import de.stroebele.mindyourself.data.db.dao.HydrationPortionSizeDao
import de.stroebele.mindyourself.data.db.entity.HydrationPortionSizeEntity
import de.stroebele.mindyourself.domain.model.HydrationPortionSize
import de.stroebele.mindyourself.domain.repository.HydrationPortionSizeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HydrationPortionSizeRepositoryImpl @Inject constructor(
    private val dao: HydrationPortionSizeDao,
) : HydrationPortionSizeRepository {

    override fun observeAll(): Flow<List<HydrationPortionSize>> =
        dao.observeAll().map { it.map { e -> HydrationPortionSize(id = e.id, amountMl = e.amountMl) } }

    override suspend fun getAll(): List<HydrationPortionSize> =
        dao.getAll().map { HydrationPortionSize(id = it.id, amountMl = it.amountMl) }

    override suspend fun save(size: HydrationPortionSize) {
        dao.insert(HydrationPortionSizeEntity(id = size.id, amountMl = size.amountMl))
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}
