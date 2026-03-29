package de.stroebele.mindyourself.data.repository

import android.util.Log
import androidx.room.withTransaction
import de.stroebele.mindyourself.data.db.AppDatabase
import de.stroebele.mindyourself.data.db.dao.HydrationPortionSizeDao
import de.stroebele.mindyourself.data.db.dao.NamedLocationDao
import de.stroebele.mindyourself.data.db.dao.ReminderConfigDao
import de.stroebele.mindyourself.data.db.entity.HydrationPortionSizeEntity
import de.stroebele.mindyourself.data.db.entity.NamedLocationEntity
import de.stroebele.mindyourself.data.db.entity.ReminderConfigEntity
import de.stroebele.mindyourself.domain.model.AppSettings
import de.stroebele.mindyourself.domain.model.VacationPeriod
import de.stroebele.mindyourself.domain.model.VacationSettings
import de.stroebele.mindyourself.domain.repository.AppSettingsRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class BackupRepository @Inject constructor(
    private val db: AppDatabase,
    private val reminderConfigDao: ReminderConfigDao,
    private val namedLocationDao: NamedLocationDao,
    private val hydrationPortionSizeDao: HydrationPortionSizeDao,
    private val vacationSettingsRepository: VacationSettingsRepository,
    private val appSettingsRepository: AppSettingsRepository,
) {

    suspend fun exportAsJson(): String {
        val root = JSONObject()
        root.put("version", 1)

        // 0. App settings
        val appSettings = appSettingsRepository.observe().first()
        root.put("step_daily_goal", appSettings.stepDailyGoal)

        // 1. Reminder configs
        val reminderArray = JSONArray()
        for (e in reminderConfigDao.getAll()) {
            val obj = JSONObject()
            obj.put("type", e.type)
            obj.put("enabled", e.enabled)
            obj.put("label", e.label)
            obj.put("activeDays", e.activeDays)
            obj.put("activeFromHour", e.activeFromHour)
            obj.put("activeFromMinute", e.activeFromMinute)
            obj.put("activeUntilHour", e.activeUntilHour)
            obj.put("activeUntilMinute", e.activeUntilMinute)
            obj.put("typeConfigJson", e.typeConfigJson)
            obj.put("activeInVacation", e.activeInVacation)
            if (e.locationFilterJson != null) obj.put("locationFilterJson", e.locationFilterJson)
            reminderArray.put(obj)
        }
        root.put("reminder_configs", reminderArray)

        // 2. Named locations
        val locationArray = JSONArray()
        for (loc in namedLocationDao.getAll()) {
            val obj = JSONObject()
            obj.put("name", loc.name)
            obj.put("wifiSsid", if (loc.wifiSsid != null) loc.wifiSsid else JSONObject.NULL)
            val cellIdsArray = JSONArray()
            if (loc.cellIds.isNotBlank()) loc.cellIds.split(",").forEach { cellIdsArray.put(it.trim()) }
            obj.put("cellIds", cellIdsArray)
            locationArray.put(obj)
        }
        root.put("locations", locationArray)

        // 3. Vacation periods
        val vacationArray = JSONArray()
        val vacation = vacationSettingsRepository.observe().first()
        for (period in vacation.periods) {
            val obj = JSONObject()
            obj.put("from", period.from.toEpochMs())
            obj.put("until", period.until.toEpochMs())
            vacationArray.put(obj)
        }
        root.put("vacation_periods", vacationArray)

        // 4. Hydration portion sizes
        val portionArray = JSONArray()
        for (portion in hydrationPortionSizeDao.getAll()) {
            portionArray.put(portion.amountMl)
        }
        root.put("hydration_portion_sizes", portionArray)

        return root.toString(2)
    }

    data class ImportResult(
        val remindersImported: Int,
        val locationsImported: Int,
        val vacationPeriodsImported: Int,
        val portionSizesImported: Int,
        val stepGoalImported: Boolean = false,
    )

    suspend fun importFromJson(json: String): ImportResult {
        val root = JSONObject(json)

        var remindersImported = 0
        var locationsImported = 0
        var vacationPeriodsImported = 0
        var portionSizesImported = 0
        var stepGoalImported = false

        // 0. App settings
        if (root.has("step_daily_goal")) {
            runCatching {
                val goal = root.getInt("step_daily_goal")
                val current = appSettingsRepository.observe().first()
                appSettingsRepository.save(current.copy(stepDailyGoal = goal))
                stepGoalImported = true
            }.onFailure { e -> Log.w(TAG, "Skipping malformed step_daily_goal", e) }
        }

        // 1. Reminder configs
        val reminderEntities = mutableListOf<ReminderConfigEntity>()
        if (root.has("reminder_configs")) {
            val array = root.getJSONArray("reminder_configs")
            for (i in 0 until array.length()) {
                runCatching {
                    val obj = array.getJSONObject(i)
                    reminderEntities.add(
                        ReminderConfigEntity(
                            id = 0,
                            type = obj.getString("type"),
                            enabled = obj.getBoolean("enabled"),
                            label = obj.getString("label"),
                            activeDays = obj.getString("activeDays"),
                            activeFromHour = obj.getInt("activeFromHour"),
                            activeFromMinute = obj.getInt("activeFromMinute"),
                            activeUntilHour = obj.getInt("activeUntilHour"),
                            activeUntilMinute = obj.getInt("activeUntilMinute"),
                            typeConfigJson = obj.getString("typeConfigJson"),
                            activeInVacation = obj.getBoolean("activeInVacation"),
                            locationFilterJson = if (obj.has("locationFilterJson")) obj.getString("locationFilterJson") else null,
                        )
                    )
                    remindersImported++
                }.onFailure { e -> Log.w(TAG, "Skipping malformed reminder at index $i", e) }
            }
        }

        // 2. Named locations
        val locationEntities = mutableListOf<NamedLocationEntity>()
        if (root.has("locations")) {
            val array = root.getJSONArray("locations")
            for (i in 0 until array.length()) {
                runCatching {
                    val obj = array.getJSONObject(i)
                    val name = obj.getString("name").trim()
                    val wifiSsid = if (obj.isNull("wifiSsid")) null else obj.optString("wifiSsid").ifBlank { null }
                    val cellIdsArray = obj.getJSONArray("cellIds")
                    val cellIds = (0 until cellIdsArray.length()).joinToString(",") { cellIdsArray.getString(it) }
                    locationEntities.add(NamedLocationEntity(name = name, wifiSsid = wifiSsid, cellIds = cellIds))
                    locationsImported++
                }.onFailure { e -> Log.w(TAG, "Skipping malformed location at index $i", e) }
            }
        }

        // 3. Vacation periods
        val periods = mutableListOf<VacationPeriod>()
        if (root.has("vacation_periods")) {
            val array = root.getJSONArray("vacation_periods")
            for (i in 0 until array.length()) {
                runCatching {
                    val obj = array.getJSONObject(i)
                    periods.add(
                        VacationPeriod(
                            from = obj.getLong("from").toLocalDateTime(),
                            until = obj.getLong("until").toLocalDateTime(),
                        )
                    )
                    vacationPeriodsImported++
                }.onFailure { e -> Log.w(TAG, "Skipping malformed vacation period at index $i", e) }
            }
        }

        // 4. Hydration portion sizes
        val portionEntities = mutableListOf<HydrationPortionSizeEntity>()
        if (root.has("hydration_portion_sizes")) {
            val array = root.getJSONArray("hydration_portion_sizes")
            for (i in 0 until array.length()) {
                runCatching {
                    portionEntities.add(HydrationPortionSizeEntity(id = 0, amountMl = array.getInt(i)))
                    portionSizesImported++
                }.onFailure { e -> Log.w(TAG, "Skipping malformed portion size at index $i", e) }
            }
        }

        // Apply all in one transaction for reminder configs and locations
        db.withTransaction {
            if (reminderEntities.isNotEmpty()) {
                reminderConfigDao.deleteAll()
                reminderConfigDao.upsertAll(reminderEntities)
            }
            if (locationEntities.isNotEmpty()) {
                namedLocationDao.deleteAll()
                namedLocationDao.upsertAll(locationEntities)
            }
            if (portionEntities.isNotEmpty()) {
                hydrationPortionSizeDao.deleteAll()
                hydrationPortionSizeDao.insertAll(portionEntities)
            }
        }

        // Vacation settings via DataStore (outside transaction)
        if (vacationPeriodsImported > 0) {
            vacationSettingsRepository.save(VacationSettings(periods = periods))
        }

        return ImportResult(remindersImported, locationsImported, vacationPeriodsImported, portionSizesImported, stepGoalImported)
    }

    private fun LocalDateTime.toEpochMs(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

    companion object {
        private const val TAG = "BackupRepository"
    }
}
