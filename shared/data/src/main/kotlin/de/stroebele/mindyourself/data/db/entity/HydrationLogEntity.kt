package de.stroebele.mindyourself.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hydration_logs",
    indices = [Index(value = ["healthConnectId"], unique = true)]
)
data class HydrationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val timestampEpochMs: Long,
    val synced: Boolean = false,
    val healthConnectId: String? = null,
)
