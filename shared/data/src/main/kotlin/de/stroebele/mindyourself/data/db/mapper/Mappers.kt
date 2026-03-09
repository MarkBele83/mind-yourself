package de.stroebele.mindyourself.data.db.mapper

import de.stroebele.mindyourself.data.db.entity.HealthCacheEntity
import de.stroebele.mindyourself.data.db.entity.HeartRateCacheEntity
import de.stroebele.mindyourself.data.db.entity.HydrationLogEntity
import de.stroebele.mindyourself.data.db.entity.NamedLocationEntity
import de.stroebele.mindyourself.data.db.entity.ReminderConfigEntity
import de.stroebele.mindyourself.data.db.entity.SupplementLogEntity
import de.stroebele.mindyourself.domain.model.ActivityState
import de.stroebele.mindyourself.domain.model.HealthCache
import de.stroebele.mindyourself.domain.model.HeartRateEntry
import de.stroebele.mindyourself.domain.model.HydrationLog
import de.stroebele.mindyourself.domain.model.LocationFilter
import de.stroebele.mindyourself.domain.model.MovementConfig
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.ReminderTypeConfig
import de.stroebele.mindyourself.domain.model.ScreenBreakConfig
import de.stroebele.mindyourself.domain.model.SedentaryConfig
import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.SupplementConfig
import de.stroebele.mindyourself.domain.model.SupplementLog
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

// ── HydrationLog ─────────────────────────────────────────────────────────────

fun HydrationLogEntity.toDomain() = HydrationLog(
    id = id,
    amountMl = amountMl,
    timestamp = Instant.ofEpochMilli(timestampEpochMs),
    synced = synced,
)

// ── SupplementLog ─────────────────────────────────────────────────────────────

fun SupplementLogEntity.toDomain() = SupplementLog(
    id = id,
    supplementName = supplementName,
    takenAt = Instant.ofEpochMilli(takenAtEpochMs),
    synced = synced,
)

// ── HeartRateEntry ────────────────────────────────────────────────────────────

fun HeartRateCacheEntity.toDomain() = HeartRateEntry(
    id = id,
    bpm = bpm,
    timestamp = Instant.ofEpochMilli(timestampEpochMs),
    synced = synced,
)

// ── HealthCache ───────────────────────────────────────────────────────────────

fun HealthCacheEntity.toDomain() = HealthCache(
    id = id,
    steps = steps,
    activityState = ActivityState.valueOf(activityState),
    timestamp = Instant.ofEpochMilli(timestampEpochMs),
)

// ── NamedLocation ─────────────────────────────────────────────────────────────

fun NamedLocationEntity.toDomain() = NamedLocation(
    id = id,
    name = name,
    wifiSsid = wifiSsid,
    cellIds = if (cellIds.isBlank()) emptyList() else cellIds.split(","),
)

fun NamedLocation.toEntity() = NamedLocationEntity(
    id = id,
    name = name,
    wifiSsid = wifiSsid,
    cellIds = cellIds.joinToString(","),
)

// ── ReminderConfig ────────────────────────────────────────────────────────────

fun ReminderConfigEntity.toDomain(): ReminderConfig = ReminderConfig(
    id = id,
    type = ReminderType.valueOf(type),
    enabled = enabled,
    label = label,
    activeDays = activeDays.split(",").map { DayOfWeek.valueOf(it.trim()) }.toSet(),
    activeFrom = LocalTime.of(activeFromHour, activeFromMinute),
    activeUntil = LocalTime.of(activeUntilHour, activeUntilMinute),
    typeConfig = deserializeTypeConfig(ReminderType.valueOf(type), typeConfigJson),
    activeInVacation = activeInVacation,
    locationFilter = locationFilterJson?.let { deserializeLocationFilter(it) },
)

fun ReminderConfig.toEntity(): ReminderConfigEntity = ReminderConfigEntity(
    id = id,
    type = type.name,
    enabled = enabled,
    label = label,
    activeDays = activeDays.joinToString(",") { it.name },
    activeFromHour = activeFrom.hour,
    activeFromMinute = activeFrom.minute,
    activeUntilHour = activeUntil.hour,
    activeUntilMinute = activeUntil.minute,
    typeConfigJson = serializeTypeConfig(typeConfig),
    activeInVacation = activeInVacation,
    locationFilterJson = locationFilter?.let { serializeLocationFilter(it) },
)

// ── TypeConfig serialization (manual JSON — no extra library needed) ──────────

fun serializeTypeConfig(config: ReminderTypeConfig): String = when (config) {
    is MovementConfig ->
        """{"stepThreshold":${config.stepThreshold},"windowMinutes":${config.windowMinutes}}"""
    is SedentaryConfig ->
        """{"inactiveThresholdMinutes":${config.inactiveThresholdMinutes},"repeatIntervalMinutes":${config.repeatIntervalMinutes}}"""
    is HydrationConfig ->
        """{"dailyGoalMl":${config.dailyGoalMl},"intervalMinutes":${config.intervalMinutes}}"""
    is SupplementConfig -> {
        val times = config.scheduledTimes.joinToString(",") { "\"${it.hour}:${it.minute}\"" }
        """{"supplementName":"${config.supplementName}","scheduledTimes":[$times],"snoozeDurationMinutes":${config.snoozeDurationMinutes}}"""
    }
    is ScreenBreakConfig ->
        """{"intervalMinutes":${config.intervalMinutes},"breakDurationMinutes":${config.breakDurationMinutes}}"""
}

fun deserializeTypeConfig(type: ReminderType, json: String): ReminderTypeConfig {
    fun String.extract(key: String): String =
        Regex(""""$key"\s*:\s*([^,}\]]+)""").find(this)?.groupValues?.get(1)?.trim()?.removeSurrounding("\"")
            ?: error("Missing key '$key' in $this")

    return when (type) {
        ReminderType.MOVEMENT -> MovementConfig(
            stepThreshold = json.extract("stepThreshold").toInt(),
            windowMinutes = json.extract("windowMinutes").toInt(),
        )
        ReminderType.SEDENTARY -> SedentaryConfig(
            inactiveThresholdMinutes = json.extract("inactiveThresholdMinutes").toInt(),
            repeatIntervalMinutes = json.extract("repeatIntervalMinutes").toInt(),
        )
        ReminderType.HYDRATION -> HydrationConfig(
            dailyGoalMl = json.extract("dailyGoalMl").toInt(),
            intervalMinutes = json.extract("intervalMinutes").toInt(),
        )
        ReminderType.SUPPLEMENT -> {
            val timesRaw = Regex(""""scheduledTimes"\s*:\s*\[([^\]]*)]""").find(json)
                ?.groupValues?.get(1).orEmpty()
            val times = timesRaw.split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }
                .map { LocalTime.of(it.substringBefore(":").toInt(), it.substringAfter(":").toInt()) }
            SupplementConfig(
                supplementName = json.extract("supplementName"),
                scheduledTimes = times,
                snoozeDurationMinutes = json.extract("snoozeDurationMinutes").toInt(),
            )
        }
        ReminderType.SCREEN_BREAK -> ScreenBreakConfig(
            intervalMinutes = json.extract("intervalMinutes").toInt(),
            breakDurationMinutes = json.extract("breakDurationMinutes").toInt(),
        )
    }
}

// ── LocationFilter serialization ──────────────────────────────────────────────

fun serializeLocationFilter(filter: LocationFilter): String {
    val ids = filter.allowedLocationIds.joinToString(",")
    return """{"allowedLocationIds":[$ids],"strictMode":${filter.strictMode}}"""
}

fun deserializeLocationFilter(json: String): LocationFilter {
    val idsRaw = Regex(""""allowedLocationIds"\s*:\s*\[([^\]]*)]""").find(json)
        ?.groupValues?.get(1).orEmpty()
    val ids = idsRaw.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
    val strict = Regex(""""strictMode"\s*:\s*(true|false)""").find(json)
        ?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: true
    return LocationFilter(allowedLocationIds = ids, strictMode = strict)
}
