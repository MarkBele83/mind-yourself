package de.stroebele.mindyourself.sync.model

/**
 * Manual JSON serialization for sync DTOs.
 * Keeps the :shared:sync module dependency-free beyond play-services-wearable.
 */
object SyncSerializer {

    // ── ReminderConfigDto ─────────────────────────────────────────────────────

    fun List<ReminderConfigDto>.toJsonBytes(): ByteArray =
        joinToString(",", "[", "]") { it.toJson() }.toByteArray(Charsets.UTF_8)

    fun ByteArray.toReminderConfigList(): List<ReminderConfigDto> {
        val json = toString(Charsets.UTF_8).trim().removePrefix("[").removeSuffix("]")
        if (json.isBlank()) return emptyList()
        return splitJsonObjects(json).map { parseReminderConfig(it) }
    }

    private fun ReminderConfigDto.toJson(): String =
        """{"id":$id,"type":"$type","enabled":$enabled,"label":${label.jsonStr()},"activeDays":"$activeDays","activeFromHour":$activeFromHour,"activeFromMinute":$activeFromMinute,"activeUntilHour":$activeUntilHour,"activeUntilMinute":$activeUntilMinute,"typeConfigJson":${typeConfigJson.jsonStr()},"activeInVacation":$activeInVacation}"""

    private fun parseReminderConfig(json: String) = ReminderConfigDto(
        id = json.extractLong("id"),
        type = json.extract("type"),
        enabled = json.extract("enabled").toBoolean(),
        label = json.extract("label"),
        activeDays = json.extract("activeDays"),
        activeFromHour = json.extractInt("activeFromHour"),
        activeFromMinute = json.extractInt("activeFromMinute"),
        activeUntilHour = json.extractInt("activeUntilHour"),
        activeUntilMinute = json.extractInt("activeUntilMinute"),
        typeConfigJson = json.extract("typeConfigJson"),
        activeInVacation = json.extractBoolean("activeInVacation"),
    )

    // ── HydrationLogDto ───────────────────────────────────────────────────────

    fun List<HydrationLogDto>.hydrationToJsonBytes(): ByteArray =
        joinToString(",", "[", "]") { """{"id":${it.id},"amountMl":${it.amountMl},"timestampEpochMs":${it.timestampEpochMs}}""" }
            .toByteArray(Charsets.UTF_8)

    fun ByteArray.toHydrationLogList(): List<HydrationLogDto> {
        val json = toString(Charsets.UTF_8).trim().removePrefix("[").removeSuffix("]")
        if (json.isBlank()) return emptyList()
        return splitJsonObjects(json).map {
            HydrationLogDto(
                id = it.extractLong("id"),
                amountMl = it.extractInt("amountMl"),
                timestampEpochMs = it.extractLong("timestampEpochMs"),
            )
        }
    }

    // ── SupplementLogDto ──────────────────────────────────────────────────────

    fun List<SupplementLogDto>.supplementToJsonBytes(): ByteArray =
        joinToString(",", "[", "]") { """{"id":${it.id},"supplementName":${it.supplementName.jsonStr()},"takenAtEpochMs":${it.takenAtEpochMs}}""" }
            .toByteArray(Charsets.UTF_8)

    fun ByteArray.toSupplementLogList(): List<SupplementLogDto> {
        val json = toString(Charsets.UTF_8).trim().removePrefix("[").removeSuffix("]")
        if (json.isBlank()) return emptyList()
        return splitJsonObjects(json).map {
            SupplementLogDto(
                id = it.extractLong("id"),
                supplementName = it.extract("supplementName"),
                takenAtEpochMs = it.extractLong("takenAtEpochMs"),
            )
        }
    }

    // ── HeartRateDto ──────────────────────────────────────────────────────────

    fun List<HeartRateDto>.heartRateToJsonBytes(): ByteArray =
        joinToString(",", "[", "]") { """{"id":${it.id},"bpm":${it.bpm},"timestampEpochMs":${it.timestampEpochMs}}""" }
            .toByteArray(Charsets.UTF_8)

    fun ByteArray.toHeartRateList(): List<HeartRateDto> {
        val json = toString(Charsets.UTF_8).trim().removePrefix("[").removeSuffix("]")
        if (json.isBlank()) return emptyList()
        return splitJsonObjects(json).map {
            HeartRateDto(
                id = it.extractLong("id"),
                bpm = it.extract("bpm").toDouble(),
                timestampEpochMs = it.extractLong("timestampEpochMs"),
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun String.jsonStr() = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

    private fun String.extract(key: String): String =
        Regex(""""$key"\s*:\s*(?:"((?:[^"\\]|\\.)*)"|([^,}\]]+))""")
            .find(this)?.let { it.groupValues[1].ifEmpty { it.groupValues[2].trim() } }
            ?: error("Missing key '$key' in: $this")

    private fun String.extractInt(key: String): Int = extract(key).toInt()
    private fun String.extractLong(key: String): Long = extract(key).toLong()
    private fun String.extractBoolean(key: String): Boolean = try { extract(key).toBoolean() } catch (e: Exception) { false }

    // ── VacationSettingsDto ───────────────────────────────────────────────────

    fun VacationSettingsDto.toVacationJsonBytes(): ByteArray {
        val periodsJson = periods.joinToString(",", "[", "]") {
            """{"fromEpochMs":${it.fromEpochMs},"untilEpochMs":${it.untilEpochMs}}"""
        }
        return periodsJson.toByteArray(Charsets.UTF_8)
    }

    fun ByteArray.toVacationSettings(): VacationSettingsDto {
        val json = toString(Charsets.UTF_8)
        val periodsStart = json.indexOf('[')
        val periodsEnd = json.lastIndexOf(']')
        val periods = if (periodsStart >= 0 && periodsEnd > periodsStart) {
            val body = json.substring(periodsStart + 1, periodsEnd)
            if (body.isBlank()) emptyList()
            else splitJsonObjects(body).map { obj ->
                VacationPeriodDto(
                    fromEpochMs = obj.extractLong("fromEpochMs"),
                    untilEpochMs = obj.extractLong("untilEpochMs"),
                )
            }
        } else emptyList()
        return VacationSettingsDto(periods = periods)
    }

    // ── HydrationExternalLogDto ───────────────────────────────────────────────

    fun List<HydrationExternalLogDto>.hcHydrationToJsonBytes(): ByteArray =
        joinToString(",", "[", "]") { """{"healthConnectId":${it.healthConnectId.jsonStr()},"amountMl":${it.amountMl},"timestampEpochMs":${it.timestampEpochMs}}""" }
            .toByteArray(Charsets.UTF_8)

    fun ByteArray.toHcHydrationLogList(): List<HydrationExternalLogDto> {
        val json = toString(Charsets.UTF_8).trim().removePrefix("[").removeSuffix("]")
        if (json.isBlank()) return emptyList()
        return splitJsonObjects(json).map {
            HydrationExternalLogDto(
                healthConnectId = it.extract("healthConnectId"),
                amountMl = it.extractInt("amountMl"),
                timestampEpochMs = it.extractLong("timestampEpochMs"),
            )
        }
    }

    // ── AppSettingsDto ────────────────────────────────────────────────────────

    fun AppSettingsDto.toAppSettingsJsonBytes(): ByteArray =
        """{"stepDailyGoal":$stepDailyGoal,"hydrationDailyGoalMl":$hydrationDailyGoalMl}""".toByteArray(Charsets.UTF_8)

    fun ByteArray.toAppSettings(): AppSettingsDto {
        val json = toString(Charsets.UTF_8)
        return AppSettingsDto(
            stepDailyGoal = json.extractInt("stepDailyGoal"),
            hydrationDailyGoalMl = try { json.extractInt("hydrationDailyGoalMl") } catch (e: Exception) { 0 },
        )
    }

    /** Splits a flat JSON array body into individual object strings. */
    private fun splitJsonObjects(json: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var start = -1
        for (i in json.indices) {
            when (json[i]) {
                '{' -> { if (depth++ == 0) start = i }
                '}' -> { if (--depth == 0 && start >= 0) objects.add(json.substring(start, i + 1)) }
            }
        }
        return objects
    }
}
