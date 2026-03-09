package de.stroebele.mindyourself.domain.model

import java.time.Instant

/**
 * A single heart rate sample from PassiveMonitoringClient.
 * Cached for 30 days; available for future HRV analysis.
 */
data class HeartRateEntry(
    val id: Long = 0,
    val bpm: Double,
    val timestamp: Instant = Instant.now(),
    val synced: Boolean = false,
)
