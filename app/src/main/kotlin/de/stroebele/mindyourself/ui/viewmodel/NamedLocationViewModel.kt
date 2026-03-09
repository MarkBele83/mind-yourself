package de.stroebele.mindyourself.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.domain.repository.NamedLocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NamedLocationEditUiState(
    val location: NamedLocation = NamedLocation(name = ""),
    val nameError: String? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class NamedLocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val namedLocationRepository: NamedLocationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val locationId: Long = savedStateHandle["locationId"] ?: 0L

    private val _uiState = MutableStateFlow(NamedLocationEditUiState())
    val uiState: StateFlow<NamedLocationEditUiState> = _uiState.asStateFlow()

    init {
        if (locationId != 0L) {
            viewModelScope.launch {
                val loc = namedLocationRepository.getById(locationId)
                if (loc != null) _uiState.update { it.copy(location = loc) }
            }
        }
    }

    fun update(location: NamedLocation) {
        _uiState.update { it.copy(location = location, nameError = null) }
    }

    fun captureCurrentWifi() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            ?: return
        val ssid = wifiManager.connectionInfo?.ssid?.removeSurrounding("\"")
            ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
            ?: return
        _uiState.update { it.copy(location = it.location.copy(wifiSsid = ssid)) }
    }

    fun captureCurrentCellIds() {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) return
        if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) return
        val telephonyManager = context.getSystemService(TelephonyManager::class.java) ?: return
        val cellIds = telephonyManager.allCellInfo
            ?.mapNotNull { it.toCellIdString() }
            ?: return
        _uiState.update { it.copy(location = it.location.copy(cellIds = cellIds)) }
    }

    fun save() {
        val loc = _uiState.value.location
        if (loc.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name darf nicht leer sein") }
            return
        }
        if (loc.wifiSsid == null && loc.cellIds.isEmpty()) {
            _uiState.update { it.copy(nameError = "Mindestens WLAN oder Mobilfunk muss erfasst sein") }
            return
        }
        viewModelScope.launch {
            val taken = namedLocationRepository.isNameTaken(loc.name.trim(), excludeId = loc.id)
            if (taken) {
                _uiState.update { it.copy(nameError = "Dieser Name ist bereits vergeben") }
                return@launch
            }
            namedLocationRepository.save(loc.copy(name = loc.name.trim()))
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun delete() {
        if (locationId == 0L) return
        viewModelScope.launch {
            namedLocationRepository.delete(locationId)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun CellInfo.toCellIdString(): String? = when (this) {
    is CellInfoLte -> {
        val id = cellIdentity
        "${id.mccString}-${id.mncString}-${id.tac}-${id.ci}"
            .takeIf { !it.contains("null") }
    }
    is CellInfoGsm -> {
        val id = cellIdentity
        "${id.mccString}-${id.mncString}-${id.lac}-${id.cid}"
            .takeIf { !it.contains("null") && id.cid != Int.MAX_VALUE }
    }
    is CellInfoWcdma -> {
        val id = cellIdentity
        "${id.mccString}-${id.mncString}-${id.lac}-${id.cid}"
            .takeIf { !it.contains("null") && id.cid != Int.MAX_VALUE }
    }
    is CellInfoCdma -> null
    else -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("NewApi")
            val nrId = (this as? android.telephony.CellInfoNr)
                ?.cellIdentity as? android.telephony.CellIdentityNr
            nrId?.let { "${it.mccString}-${it.mncString}-${it.tac}-${it.nci}" }
                ?.takeIf { !it.contains("null") }
        } else null
    }
}
