package de.stroebele.mindyourself.domain.model

import java.time.Instant

data class SupplementLog(
    val id: Long = 0,
    val supplementName: String,
    val takenAt: Instant = Instant.now(),
    /** True once synced to the phone app */
    val synced: Boolean = false,
)
