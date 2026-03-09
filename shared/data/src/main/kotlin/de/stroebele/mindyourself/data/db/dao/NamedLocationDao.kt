package de.stroebele.mindyourself.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.stroebele.mindyourself.data.db.entity.NamedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NamedLocationDao {

    @Query("SELECT * FROM named_locations ORDER BY name ASC")
    fun observeAll(): Flow<List<NamedLocationEntity>>

    @Query("SELECT * FROM named_locations ORDER BY name ASC")
    suspend fun getAll(): List<NamedLocationEntity>

    @Query("SELECT * FROM named_locations WHERE id = :id")
    suspend fun getById(id: Long): NamedLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NamedLocationEntity): Long

    @Query("DELETE FROM named_locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM named_locations WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long): Int
}
