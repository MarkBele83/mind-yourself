package de.stroebele.mindyourself.domain.model

import java.time.Instant

data class ReminderState(
    val reminderType: ReminderType,
    val lastFired: Instant = Instant.EPOCH,
    val snoozeUntil: Instant = Instant.EPOCH,
) {
    val isSnoozed: Boolean get() = snoozeUntil.isAfter(Instant.now())
}
