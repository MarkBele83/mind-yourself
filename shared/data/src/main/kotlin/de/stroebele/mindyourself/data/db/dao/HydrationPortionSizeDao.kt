package de.stroebele.mindyourself.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.stroebele.mindyourself.data.db.entity.HydrationPortionSizeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationPortionSizeDao {

    @Query("SELECT * FROM hydration_portion_sizes ORDER BY amountMl ASC")
    fun observeAll(): Flow<List<HydrationPortionSizeEntity>>

    @Query("SELECT * FROM hydration_portion_sizes ORDER BY amountMl ASC")
    suspend fun getAll(): List<HydrationPortionSizeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HydrationPortionSizeEntity): Long

    @Query("DELETE FROM hydration_portion_sizes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM hydration_portion_sizes")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<HydrationPortionSizeEntity>)
}
