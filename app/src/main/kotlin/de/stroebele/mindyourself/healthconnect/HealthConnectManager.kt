package de.stroebele.mindyourself.healthconnect

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Volume
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.domain.model.HydrationLog
import de.stroebele.mindyourself.sync.model.HydrationExternalLogDto
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("health_connect", Context.MODE_PRIVATE)
    }

    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
    )

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private fun client(): HealthConnectClient? =
        if (isAvailable()) HealthConnectClient.getOrCreate(context) else null

    suspend fun hasPermissions(): Boolean {
        val c = client() ?: return false
        return try {
            c.permissionController.getGrantedPermissions().containsAll(requiredPermissions)
        } catch (e: Exception) {
            Log.w(TAG, "Could not check HC permissions", e)
            false
        }
    }

    /**
     * Writes hydration logs to Health Connect using clientRecordId for idempotent upserts.
     * Silently skips if HC is unavailable or permissions are missing.
     */
    suspend fun writeHydrationLogs(logs: List<HydrationLog>) {
        if (logs.isEmpty()) return
        val c = client() ?: return
        try {
            val records = logs.map { log ->
                val ts = log.timestamp
                val offset = ZoneId.systemDefault().rules.getOffset(ts)
                HydrationRecord(
                    startTime = ts,
                    startZoneOffset = offset,
                    endTime = ts,
                    endZoneOffset = offset,
                    volume = Volume.milliliters(log.amountMl.toDouble()),
                    metadata = Metadata.manualEntry(
                        clientRecordId = "mindyourself-${log.id}-${ts.toEpochMilli()}",
                        device = Device(type = Device.TYPE_WATCH),
                    ),
                )
            }
            val response = c.insertRecords(records)
            Log.i(TAG, "Wrote ${response.recordIdsList.size} hydration records to HC")
        } catch (e: SecurityException) {
            Log.w(TAG, "HC permissions not granted — skipping write", e)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "HC unavailable — skipping write", e)
        } catch (e: Exception) {
            Log.e(TAG, "HC write failed", e)
        }
    }

    /**
     * Reads hydration logs from Health Connect that were not written by this app.
     * Uses Changelog API for incremental sync; falls back to full read on token expiry.
     * Returns empty list if HC is unavailable or permissions are missing.
     */
    suspend fun readExternalHydrationLogs(): List<HydrationExternalLogDto> {
        val c = client() ?: return emptyList()
        if (!hasPermissions()) return emptyList()

        return try {
            val storedToken = prefs.getString(PREF_CHANGES_TOKEN, null)
            if (storedToken != null) {
                readExternalViaChangelog(c, storedToken)
            } else {
                readExternalFull(c)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "HC permissions not granted — skipping read", e)
            emptyList()
        } catch (e: IllegalStateException) {
            Log.w(TAG, "HC unavailable or invalid state — skipping read", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "HC read failed", e)
            emptyList()
        }
    }

    private suspend fun readExternalViaChangelog(
        c: HealthConnectClient,
        token: String,
    ): List<HydrationExternalLogDto> {
        val result = mutableListOf<HydrationExternalLogDto>()
        return try {
            var nextToken = token
            do {
                val response = c.getChanges(nextToken)
                response.changes.forEach { change ->
                    when (change) {
                        is UpsertionChange -> {
                            val record = change.record
                            if (record is HydrationRecord &&
                                record.metadata.dataOrigin.packageName != context.packageName
                            ) {
                                result += HydrationExternalLogDto(
                                    healthConnectId = record.metadata.id,
                                    amountMl = record.volume.inMilliliters.toInt(),
                                    timestampEpochMs = record.startTime.toEpochMilli(),
                                )
                            }
                        }
                        is DeletionChange -> {
                            // Deletion tracking is handled via healthConnectId on the watch side.
                            // For MVP: log and ignore — the watch will keep the local copy.
                            Log.d(TAG, "HC deletion: id=${change.recordId}")
                        }
                        else -> Unit
                    }
                }
                nextToken = response.nextChangesToken
            } while (response.hasMore)
            prefs.edit().putString(PREF_CHANGES_TOKEN, nextToken).apply()
            Log.i(TAG, "Read ${result.size} external HC hydration changes via changelog")
            result
        } catch (e: IllegalStateException) {
            // Token expired — fall back to full read
            Log.w(TAG, "HC changes token expired, falling back to full read", e)
            readExternalFull(c)
        }
    }

    private suspend fun readExternalFull(c: HealthConnectClient): List<HydrationExternalLogDto> {
        val endTime = Instant.now()
        val startTime = endTime.minus(30, ChronoUnit.DAYS)
        val result = mutableListOf<HydrationExternalLogDto>()

        var pageToken: String? = null
        do {
            val response = c.readRecords(
                ReadRecordsRequest(
                    recordType = HydrationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    pageToken = pageToken,
                )
            )
            response.records.forEach { record ->
                if (record.metadata.dataOrigin.packageName != context.packageName) {
                    result += HydrationExternalLogDto(
                        healthConnectId = record.metadata.id,
                        amountMl = record.volume.inMilliliters.toInt(),
                        timestampEpochMs = record.startTime.toEpochMilli(),
                    )
                }
            }
            pageToken = response.pageToken
        } while (pageToken != null)

        // Obtain and store a fresh changes token for future incremental syncs
        val newToken = c.getChangesToken(
            ChangesTokenRequest(recordTypes = setOf(HydrationRecord::class))
        )
        prefs.edit().putString(PREF_CHANGES_TOKEN, newToken).apply()
        Log.i(TAG, "Read ${result.size} external HC hydration records (full read), token stored")
        return result
    }

    companion object {
        private const val TAG = "HealthConnectManager"
        private const val PREF_CHANGES_TOKEN = "hc_hydration_changes_token"
    }
}
