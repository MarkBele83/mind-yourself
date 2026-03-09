package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.ReminderConfig
import kotlinx.coroutines.flow.Flow

interface ReminderConfigRepository {
    fun observeAll(): Flow<List<ReminderConfig>>
    suspend fun getAll(): List<ReminderConfig>
    suspend fun getById(id: Long): ReminderConfig?
    suspend fun save(config: ReminderConfig): Long
    suspend fun delete(id: Long)
    /** Atomically replace all configs — used when syncing from Phone. */
    suspend fun replaceAll(configs: List<ReminderConfig>)
}
