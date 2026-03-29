package de.stroebele.mindyourself.data.db.mapper

import de.stroebele.mindyourself.data.db.entity.HealthCacheEntity
import de.stroebele.mindyourself.data.db.entity.HeartRateCacheEntity
import de.stroebele.mindyourself.data.db.entity.HydrationLogEntity
import de.stroebele.mindyourself.data.db.entity.NamedLocationEntity
import de.stroebele.mindyourself.data.db.entity.ReminderConfigEntity
import de.stroebele.mindyourself.data.db.entity.SupplementLogEntity
import de.stroebele.mindyourself.domain.model.ActivityState
import de.stroebele.mindyourself.domain.model.HealthCache
import de.stroebele.mindyourself.domain.model.SupplementForm
import de.stroebele.mindyourself.domain.model.SupplementItem
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
    healthConnectId = healthConnectId,
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
        """{"stepThreshold":${config.stepThreshold},"windowMinutes":${config.windowMinutes},"repeatIntervalMinutes":${config.repeatIntervalMinutes}}"""
    is SedentaryConfig ->
        """{"inactiveThresholdMinutes":${config.inactiveThresholdMinutes},"repeatIntervalMinutes":${config.repeatIntervalMinutes}}"""
    is HydrationConfig ->
        """{"reminderGoalMl":${config.reminderGoalMl},"repeatIntervalMinutes":${config.repeatIntervalMinutes},"noLogWindowMinutes":${config.noLogWindowMinutes}}"""
    is SupplementConfig -> {
        val items = config.items.joinToString(",") {
            """{"name":"${it.name}","amount":${it.amount},"form":"${it.form.name}"}"""
        }
        """{"scheduledTime":"${config.scheduledTime.hour}:${config.scheduledTime.minute}","items":[$items],"snoozeDurationMinutes":${config.snoozeDurationMinutes}}"""
    }
    is ScreenBreakConfig ->
        """{"intervalMinutes":${config.intervalMinutes},"breakDurationMinutes":${config.breakDurationMinutes}}"""
}

/**
 * Deserializes a [ReminderTypeConfig] from its stored JSON string.
 *
 * ## Backward-compatibility contract
 * Every field extraction uses [runCatching] with a sensible default so that:
 * - Old rows missing a newly-added field still load (default applied).
 * - Renamed fields can be handled by trying both old and new key names.
 * - A completely unreadable row returns the type's factory default rather than
 *   crashing. The caller ([ReminderConfigRepositoryImpl]) logs and skips truly
 *   unrecoverable rows, so one corrupt config never takes down the rest.
 *
 * ## Developer checklist when changing a config data class
 * 1. Add the new field to [serializeTypeConfig] — it will be written on next save.
 * 2. Add `runCatching { json.extract("newField")... }.getOrElse { defaultValue }`
 *    in the matching branch below — old rows without the key get the default.
 * 3. If you *rename* a key, try the new name first and fall back to the old name:
 *    `runCatching { json.extract("newKey")... }.getOrElse { runCatching { json.extract("oldKey")... }.getOrElse { default } }`
 * 4. If the DB *schema* changes (new column, new table) bump [AppDatabase.version]
 *    and add a Migration object — never call fallbackToDestructiveMigration().
 */
fun deserializeTypeConfig(type: ReminderType, json: String): ReminderTypeConfig {
    // Returns the raw string value for `key`, or null if absent — never throws.
    fun String.extractOrNull(key: String): String? =
        Regex(""""$key"\s*:\s*([^,}\]]+)""").find(this)
            ?.groupValues?.get(1)?.trim()?.removeSurrounding("\"")

    fun String.extractInt(key: String, default: Int): Int =
        runCatching { extractOrNull(key)?.toInt() ?: default }.getOrElse { default }

    return when (type) {
        ReminderType.MOVEMENT -> MovementConfig(
            stepThreshold = json.extractInt("stepThreshold", 500),
            windowMinutes = json.extractInt("windowMinutes", 30),
            repeatIntervalMinutes = json.extractInt("repeatIntervalMinutes", 30),
        )
        ReminderType.SEDENTARY -> SedentaryConfig(
            inactiveThresholdMinutes = json.extractInt("inactiveThresholdMinutes", 45),
            repeatIntervalMinutes = json.extractInt("repeatIntervalMinutes", 60),
        )
        ReminderType.HYDRATION -> HydrationConfig(
            reminderGoalMl = runCatching { json.extractOrNull("reminderGoalMl")?.toInt() }.getOrNull()
                ?: runCatching { json.extractOrNull("dailyGoalMl")?.toInt() }.getOrNull()
                ?: 2000,
            repeatIntervalMinutes = json.extractInt("repeatIntervalMinutes", 60),
            noLogWindowMinutes = json.extractInt("noLogWindowMinutes", 60),
        )
        ReminderType.SUPPLEMENT -> {
            val scheduledTime = runCatching {
                val raw = json.extractOrNull("scheduledTime") ?: "8:0"
                LocalTime.of(raw.substringBefore(":").toInt(), raw.substringAfter(":").toInt())
            }.getOrElse { LocalTime.of(8, 0) }

            val itemsRaw = Regex(""""items"\s*:\s*\[([^\]]*)]""").find(json)
                ?.groupValues?.get(1).orEmpty()
            val items = Regex("""\{[^}]+\}""").findAll(itemsRaw).mapNotNull { m ->
                runCatching {
                    val obj = m.value
                    SupplementItem(
                        name = obj.extractOrNull("name") ?: return@runCatching null,
                        amount = obj.extractInt("amount", 1),
                        form = runCatching {
                            SupplementForm.valueOf(obj.extractOrNull("form") ?: "CAPSULE")
                        }.getOrElse { SupplementForm.CAPSULE },
                    )
                }.getOrNull()
            }.toList()

            SupplementConfig(
                scheduledTime = scheduledTime,
                items = items,
                snoozeDurationMinutes = json.extractInt("snoozeDurationMinutes", 30),
            )
        }
        ReminderType.SCREEN_BREAK -> ScreenBreakConfig(
            intervalMinutes = json.extractInt("intervalMinutes", 45),
            breakDurationMinutes = json.extractInt("breakDurationMinutes", 5),
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
