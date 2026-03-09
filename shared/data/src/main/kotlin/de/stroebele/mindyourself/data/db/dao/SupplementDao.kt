package de.stroebele.mindyourself.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.stroebele.mindyourself.data.db.entity.SupplementLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementDao {

    @Query("SELECT * FROM supplement_logs WHERE takenAtEpochMs >= :startOfDayMs ORDER BY takenAtEpochMs DESC")
    fun observeToday(startOfDayMs: Long): Flow<List<SupplementLogEntity>>

    @Query("SELECT * FROM supplement_logs WHERE synced = 0")
    suspend fun getUnsynced(): List<SupplementLogEntity>

    @Insert
    suspend fun insert(entity: SupplementLogEntity): Long

    @Query("UPDATE supplement_logs SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM supplement_logs WHERE takenAtEpochMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<SupplementLogEntity>)
}
