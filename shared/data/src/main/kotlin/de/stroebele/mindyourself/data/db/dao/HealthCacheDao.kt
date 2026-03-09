package de.stroebele.mindyourself.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.stroebele.mindyourself.data.db.entity.HealthCacheEntity
import de.stroebele.mindyourself.data.db.entity.HeartRateCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthCacheDao {

    @Query("SELECT COALESCE(SUM(steps), 0) FROM health_cache WHERE timestampEpochMs >= :startOfDayMs")
    fun observeTodaySteps(startOfDayMs: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(steps), 0) FROM health_cache WHERE timestampEpochMs BETWEEN :fromMs AND :toMs")
    suspend fun getStepsBetween(fromMs: Long, toMs: Long): Long

    /**
     * Returns the earliest timestamp where the activity state changed away from [state],
     * looking backwards from [untilMs]. Used to compute continuous duration in a state.
     */
    @Query("""
        SELECT MIN(timestampEpochMs) FROM health_cache
        WHERE timestampEpochMs <= :untilMs AND activityState != :state
        AND timestampEpochMs = (
            SELECT MAX(timestampEpochMs) FROM health_cache
            WHERE timestampEpochMs <= :untilMs AND activityState != :state
        )
    """)
    suspend fun lastStateChangeAwayFrom(state: String, untilMs: Long): Long?

    @Insert
    suspend fun insertHealthSnapshot(entity: HealthCacheEntity)

    @Insert
    suspend fun insertHeartRate(entity: HeartRateCacheEntity)

    @Query("SELECT * FROM heart_rate_cache WHERE synced = 0")
    suspend fun getUnsyncedHeartRates(): List<HeartRateCacheEntity>

    @Query("UPDATE heart_rate_cache SET synced = 1 WHERE id IN (:ids)")
    suspend fun markHeartRatesSynced(ids: List<Long>)

    @Query("DELETE FROM health_cache WHERE timestampEpochMs < :beforeMs")
    suspend fun deleteHealthOlderThan(beforeMs: Long)

    @Query("DELETE FROM heart_rate_cache WHERE timestampEpochMs < :beforeMs")
    suspend fun deleteHeartRateOlderThan(beforeMs: Long)
}
