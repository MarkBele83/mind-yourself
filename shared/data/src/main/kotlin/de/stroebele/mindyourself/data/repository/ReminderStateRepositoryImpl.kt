package de.stroebele.mindyourself.data.repository

import de.stroebele.mindyourself.data.db.dao.ReminderStateDao
import de.stroebele.mindyourself.data.db.entity.ReminderStateEntity
import de.stroebele.mindyourself.domain.model.ReminderState
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.repository.ReminderStateRepository
import java.time.Instant
import javax.inject.Inject

class ReminderStateRepositoryImpl @Inject constructor(
    private val dao: ReminderStateDao,
) : ReminderStateRepository {

    override suspend fun getState(type: ReminderType): ReminderState {
        val entity = dao.getByType(type.name) ?: return ReminderState(type)
        return ReminderState(
            reminderType = type,
            lastFired = Instant.ofEpochMilli(entity.lastFiredEpochMs),
            snoozeUntil = Instant.ofEpochMilli(entity.snoozeUntilEpochMs),
        )
    }

    override suspend fun markFired(type: ReminderType, at: Instant) {
        dao.upsert(
            dao.getByType(type.name)?.copy(lastFiredEpochMs = at.toEpochMilli())
                ?: ReminderStateEntity(reminderType = type.name, lastFiredEpochMs = at.toEpochMilli())
        )
    }

    override suspend fun snooze(type: ReminderType, until: Instant) {
        dao.upsert(
            dao.getByType(type.name)?.copy(snoozeUntilEpochMs = until.toEpochMilli())
                ?: ReminderStateEntity(reminderType = type.name, snoozeUntilEpochMs = until.toEpochMilli())
        )
    }
}
