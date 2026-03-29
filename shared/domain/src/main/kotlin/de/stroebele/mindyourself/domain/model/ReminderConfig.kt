package de.stroebele.mindyourself.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * A configured reminder on the watch.
 * Templates are predefined per [ReminderType]; the user adjusts time/goal/interval.
 */
data class ReminderConfig(
    val id: Long = 0,
    val type: ReminderType,
    val enabled: Boolean = true,
    /** Human-readable label, e.g. "Morning Water" */
    val label: String,
    /** Active days of the week */
    val activeDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    /** Quiet hours: no reminders outside this window */
    val activeFrom: LocalTime = LocalTime.of(8, 0),
    val activeUntil: LocalTime = LocalTime.of(22, 0),
    val typeConfig: ReminderTypeConfig,
    /** If true, this reminder fires even when vacation mode is active. */
    val activeInVacation: Boolean = false,
    /** Optional location constraint. Null means the reminder fires at any location. */
    val locationFilter: LocationFilter? = null,
)

enum class ReminderType {
    MOVEMENT,       // step-count below threshold in time window
    SEDENTARY,      // UserActivityInfo PASSIVE > duration
    HYDRATION,      // time-based + daily goal
    SUPPLEMENT,     // time-based per supplement item
    SCREEN_BREAK,   // interval-based
}

/** Per-type configuration — sealed so each type carries only its relevant fields. */
sealed interface ReminderTypeConfig

data class MovementConfig(
    /** Minimum steps required within [windowMinutes] */
    val stepThreshold: Int = 500,
    val windowMinutes: Int = 30,
    /** Minimum minutes between repeat reminders */
    val repeatIntervalMinutes: Int = 30,
) : ReminderTypeConfig

data class SedentaryConfig(
    /** Minutes of PASSIVE activity before reminder fires */
    val inactiveThresholdMinutes: Int = 45,
    /** Minimum minutes between repeat reminders */
    val repeatIntervalMinutes: Int = 60,
) : ReminderTypeConfig

data class HydrationConfig(
    /** Reminder goal in milliliters (per reminder window) */
    val reminderGoalMl: Int = 2000,
    /** Minimum minutes between repeat reminders */
    val repeatIntervalMinutes: Int = 60,
    /** Fire if no log recorded in the last Z minutes */
    val noLogWindowMinutes: Int = 60,
) : ReminderTypeConfig

enum class SupplementForm { CAPSULE, PILL, DROP, GUM }

data class SupplementItem(
    val name: String,
    val amount: Int = 1,
    val form: SupplementForm = SupplementForm.CAPSULE,
)

data class SupplementConfig(
    /** Time at which this reminder fires */
    val scheduledTime: LocalTime,
    /** One or more supplements to take at this time */
    val items: List<SupplementItem>,
    /** Snooze duration in minutes if user taps "Remind me later" */
    val snoozeDurationMinutes: Int = 30,
) : ReminderTypeConfig

data class ScreenBreakConfig(
    /** Reminder fires every N minutes of screen/focus time */
    val intervalMinutes: Int = 45,
    /** Duration of the suggested break in minutes */
    val breakDurationMinutes: Int = 5,
) : ReminderTypeConfig
