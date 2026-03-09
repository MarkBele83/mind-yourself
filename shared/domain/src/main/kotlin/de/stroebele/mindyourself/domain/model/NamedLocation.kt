package de.stroebele.mindyourself.domain.model

/**
 * A user-defined named location identified by WLAN SSID (primary) and/or Cell IDs (fallback).
 * Configured on the Phone, synced to Watch via Data Layer.
 */
data class NamedLocation(
    val id: Long = 0,
    val name: String,
    /** Primary fingerprint: WLAN SSID. Null if only Cell ID matching is configured. */
    val wifiSsid: String? = null,
    /** Fallback fingerprint: list of Cell IDs (format: "<mcc>-<mnc>-<lac>-<cid>"). */
    val cellIds: List<String> = emptyList(),
)
