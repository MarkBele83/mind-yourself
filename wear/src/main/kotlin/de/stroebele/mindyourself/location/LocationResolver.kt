package de.stroebele.mindyourself.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.domain.model.NamedLocation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the current [NamedLocation] by matching WLAN SSID (primary)
 * or Cell IDs (fallback) against a list of user-defined locations.
 * Only called at reminder evaluation time — no continuous polling.
 */
@Singleton
class LocationResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Returns the matching [NamedLocation] for the current network context,
     * or null if no match is found or permissions are missing.
     */
    fun resolve(namedLocations: List<NamedLocation>): NamedLocation? {
        if (namedLocations.isEmpty()) return null

        val wifiMatch = resolveByWifi(namedLocations)
        if (wifiMatch != null) return wifiMatch

        return resolveByCellId(namedLocations)
    }

    private fun resolveByWifi(locations: List<NamedLocation>): NamedLocation? {
        if (!hasPermission(Manifest.permission.ACCESS_WIFI_STATE)) return null

        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            ?: return null
        val ssid = wifiManager.connectionInfo?.ssid?.removeSurrounding("\"")
            ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
            ?: return null

        return locations.firstOrNull { it.wifiSsid == ssid }
            .also { if (it != null) Log.d(TAG, "WiFi match: ${it.name} (SSID=$ssid)") }
    }

    private fun resolveByCellId(locations: List<NamedLocation>): NamedLocation? {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            Manifest.permission.ACCESS_COARSE_LOCATION
        }
        if (!hasPermission(permission)) return null
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) return null

        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            ?: return null

        val currentCellIds = telephonyManager.allCellInfo
            ?.mapNotNull { it.toCellIdString() }
            ?.toSet()
            ?: return null

        if (currentCellIds.isEmpty()) return null

        return locations.firstOrNull { location ->
            location.cellIds.any { it in currentCellIds }
        }.also { if (it != null) Log.d(TAG, "Cell ID match: ${it.name}") }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val TAG = "LocationResolver"
    }
}
