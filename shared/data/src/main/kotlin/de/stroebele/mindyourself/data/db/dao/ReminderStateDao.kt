package de.stroebele.mindyourself.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.stroebele.mindyourself.data.db.entity.ReminderStateEntity

@Dao
interface ReminderStateDao {

    @Query("SELECT * FROM reminder_state WHERE reminderType = :type")
    suspend fun getByType(type: String): ReminderStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderStateEntity)

    @Query("UPDATE reminder_state SET lastFiredEpochMs = :firedMs WHERE reminderType = :type")
    suspend fun updateLastFired(type: String, firedMs: Long)

    @Query("UPDATE reminder_state SET snoozeUntilEpochMs = :untilMs WHERE reminderType = :type")
    suspend fun updateSnooze(type: String, untilMs: Long)
}
