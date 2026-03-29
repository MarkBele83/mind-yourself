package de.stroebele.mindyourself.sync.model

/**
 * Lightweight DTOs for Watch ↔ Phone sync via Wear Data Layer API.
 * Serialized as JSON byte arrays — no external library, consistent with :shared:data mapper approach.
 * (Replace with Kotlinx Serialization if Open-Source release requires maintainability by others.)
 */

data class ReminderConfigDto(
    val id: Long,
    val type: String,
    val enabled: Boolean,
    val label: String,
    val activeDays: String,       // comma-separated DayOfWeek names
    val activeFromHour: Int,
    val activeFromMinute: Int,
    val activeUntilHour: Int,
    val activeUntilMinute: Int,
    val typeConfigJson: String,
    val activeInVacation: Boolean = false,
)

data class HydrationLogDto(
    val id: Long,
    val amountMl: Int,
    val timestampEpochMs: Long,
)

data class SupplementLogDto(
    val id: Long,
    val supplementName: String,
    val takenAtEpochMs: Long,
)

data class HeartRateDto(
    val id: Long,
    val bpm: Double,
    val timestampEpochMs: Long,
)

data class VacationPeriodDto(
    val fromEpochMs: Long,
    val untilEpochMs: Long,
)

data class VacationSettingsDto(
    val periods: List<VacationPeriodDto>,
)

data class AppSettingsDto(
    val stepDailyGoal: Int,
    val hydrationDailyGoalMl: Int = 0,
)

/** Hydration log sourced from Health Connect — Phone → Watch direction */
data class HydrationExternalLogDto(
    val healthConnectId: String,
    val amountMl: Int,
    val timestampEpochMs: Long,
)
