package de.stroebele.mindyourself.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks the last time a reminder fired and any active snooze expiry.
 * Keyed by [reminderType] (ReminderType.name) — one row per type.
 */
@Entity(tableName = "reminder_state")
data class ReminderStateEntity(
    @PrimaryKey
    val reminderType: String,
    val lastFiredEpochMs: Long = 0L,
    /** If > now, the reminder is snoozed and must not fire. */
    val snoozeUntilEpochMs: Long = 0L,
)
