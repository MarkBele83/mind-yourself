package de.stroebele.mindyourself.domain.model

import java.time.Instant

data class HydrationLog(
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: Instant = Instant.now(),
    /** True once synced to the phone app */
    val synced: Boolean = false,
)
