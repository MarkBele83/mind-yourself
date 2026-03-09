package de.stroebele.mindyourself.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.domain.model.VacationPeriod
import de.stroebele.mindyourself.domain.model.VacationSettings
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private val Context.vacationDataStore: DataStore<Preferences> by preferencesDataStore(name = "vacation_settings")

@Singleton
class VacationSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : VacationSettingsRepository {

    private val periodsJsonKey = stringPreferencesKey("vacation_periods_json")

    override fun observe(): Flow<VacationSettings> =
        context.vacationDataStore.data.map { prefs ->
            VacationSettings(
                periods = prefs[periodsJsonKey]?.parsePeriods() ?: emptyList(),
            )
        }

    override suspend fun save(settings: VacationSettings) {
        context.vacationDataStore.edit { prefs ->
            prefs[periodsJsonKey] = settings.periods.toJson()
        }
    }

    private fun List<VacationPeriod>.toJson(): String =
        joinToString(",", "[", "]") { """{"from":${it.from.toEpochMs()},"until":${it.until.toEpochMs()}}""" }

    private fun String.parsePeriods(): List<VacationPeriod> {
        val body = trim().removePrefix("[").removeSuffix("]")
        if (body.isBlank()) return emptyList()
        return splitObjects(body).mapNotNull { obj ->
            runCatching {
                VacationPeriod(
                    from = obj.extractLong("from").toLocalDateTime(),
                    until = obj.extractLong("until").toLocalDateTime(),
                )
            }.getOrNull()
        }
    }

    private fun splitObjects(json: String): List<String> {
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

    private fun String.extractLong(key: String): Long =
        Regex(""""$key"\s*:\s*(-?\d+)""").find(this)?.groupValues?.get(1)?.toLong()
            ?: error("Missing key '$key' in: $this")

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

    private fun LocalDateTime.toEpochMs(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
