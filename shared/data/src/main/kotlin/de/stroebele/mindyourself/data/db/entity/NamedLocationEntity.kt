package de.stroebele.mindyourself.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "named_locations",
    indices = [Index(value = ["name"], unique = true)],
)
data class NamedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** WLAN SSID, or null if only Cell IDs are configured. */
    val wifiSsid: String? = null,
    /** Comma-separated Cell IDs, e.g. "262-02-1234-56789,262-02-1234-56790". Empty string if none. */
    val cellIds: String = "",
)
