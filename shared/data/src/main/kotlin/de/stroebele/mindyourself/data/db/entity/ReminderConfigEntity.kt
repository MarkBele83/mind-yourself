package de.stroebele.mindyourself.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_configs")
data class ReminderConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,           // ReminderType name
    val enabled: Boolean,
    val label: String,
    val activeDays: String,     // comma-separated DayOfWeek names, e.g. "MONDAY,TUESDAY"
    val activeFromHour: Int,
    val activeFromMinute: Int,
    val activeUntilHour: Int,
    val activeUntilMinute: Int,
    /** JSON-serialized type-specific config */
    val typeConfigJson: String,
    val activeInVacation: Boolean = false,
    /** JSON-serialized LocationFilter, or null if no location constraint. */
    val locationFilterJson: String? = null,
)
