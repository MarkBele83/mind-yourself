package de.stroebele.mindyourself.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.stroebele.mindyourself.data.db.entity.HydrationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {

    @Query("SELECT * FROM hydration_logs WHERE timestampEpochMs >= :startOfDayMs ORDER BY timestampEpochMs DESC")
    fun observeToday(startOfDayMs: Long): Flow<List<HydrationLogEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM hydration_logs WHERE timestampEpochMs >= :startOfDayMs")
    suspend fun getTodayTotalMl(startOfDayMs: Long): Int

    @Query("SELECT * FROM hydration_logs WHERE synced = 0")
    suspend fun getUnsynced(): List<HydrationLogEntity>

    @Insert
    suspend fun insert(entity: HydrationLogEntity): Long

    @Query("UPDATE hydration_logs SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM hydration_logs WHERE timestampEpochMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<HydrationLogEntity>)

    @Query("DELETE FROM hydration_logs WHERE id = (SELECT id FROM hydration_logs WHERE timestampEpochMs >= :startOfDayMs AND amountMl = :amountMl ORDER BY timestampEpochMs DESC LIMIT 1)")
    suspend fun removeLatestOfAmount(amountMl: Int, startOfDayMs: Long)

    @Query("DELETE FROM hydration_logs WHERE id = (SELECT id FROM hydration_logs WHERE timestampEpochMs >= :startOfDayMs ORDER BY timestampEpochMs DESC LIMIT 1)")
    suspend fun removeLatestEntry(startOfDayMs: Long)
}
