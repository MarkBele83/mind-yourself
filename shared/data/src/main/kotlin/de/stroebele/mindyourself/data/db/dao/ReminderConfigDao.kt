package de.stroebele.mindyourself.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.stroebele.mindyourself.data.db.entity.ReminderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderConfigDao {

    @Query("SELECT * FROM reminder_configs ORDER BY label ASC")
    fun observeAll(): Flow<List<ReminderConfigEntity>>

    @Query("SELECT * FROM reminder_configs ORDER BY label ASC")
    suspend fun getAll(): List<ReminderConfigEntity>

    @Query("SELECT * FROM reminder_configs WHERE id = :id")
    suspend fun getById(id: Long): ReminderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderConfigEntity): Long

    @Query("DELETE FROM reminder_configs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reminder_configs")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ReminderConfigEntity>)
}
