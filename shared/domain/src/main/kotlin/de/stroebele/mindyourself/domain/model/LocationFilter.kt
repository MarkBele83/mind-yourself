package de.stroebele.mindyourself.domain.model

/**
 * Optional location constraint for a [ReminderConfig].
 *
 * @param allowedLocationIds IDs of [NamedLocation]s at which the reminder is active.
 * @param strictMode If true, the reminder is suppressed when the current location is unknown
 *                   (no WLAN or Cell ID match). If false, the reminder fires regardless.
 */
data class LocationFilter(
    val allowedLocationIds: Set<Long>,
    val strictMode: Boolean = true,
)
