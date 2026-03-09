package de.stroebele.mindyourself.domain.model

import java.time.Instant

/**
 * A step/activity snapshot from PassiveListenerService.
 * Stored locally for 30 days to evaluate movement reminders.
 */
data class HealthCache(
    val id: Long = 0,
    val steps: Long,
    val activityState: ActivityState,
    val timestamp: Instant = Instant.now(),
)

enum class ActivityState {
    PASSIVE,    // USER_ACTIVITY_PASSIVE
    EXERCISE,   // USER_ACTIVITY_EXERCISE
    ASLEEP,     // USER_ACTIVITY_ASLEEP
    UNKNOWN,
}
