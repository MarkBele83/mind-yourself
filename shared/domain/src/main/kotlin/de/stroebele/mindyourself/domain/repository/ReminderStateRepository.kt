package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.ReminderState
import de.stroebele.mindyourself.domain.model.ReminderType
import java.time.Instant

interface ReminderStateRepository {
    suspend fun getState(type: ReminderType): ReminderState
    suspend fun markFired(type: ReminderType, at: Instant = Instant.now())
    suspend fun snooze(type: ReminderType, until: Instant)
}
