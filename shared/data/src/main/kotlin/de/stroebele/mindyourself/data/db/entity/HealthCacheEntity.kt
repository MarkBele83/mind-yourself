package de.stroebele.mindyourself.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_cache")
data class HealthCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val steps: Long,
    val activityState: String,  // ActivityState name
    val timestampEpochMs: Long,
)

@Entity(tableName = "heart_rate_cache")
data class HeartRateCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bpm: Double,
    val timestampEpochMs: Long,
    val synced: Boolean = false,
)
