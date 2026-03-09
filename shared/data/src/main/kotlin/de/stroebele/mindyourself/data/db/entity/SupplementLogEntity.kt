package de.stroebele.mindyourself.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplement_logs")
data class SupplementLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplementName: String,
    val takenAtEpochMs: Long,
    val synced: Boolean = false,
)
