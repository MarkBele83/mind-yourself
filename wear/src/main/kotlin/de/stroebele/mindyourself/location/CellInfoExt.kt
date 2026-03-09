package de.stroebele.mindyourself.location

import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma

/**
 * Converts a [CellInfo] to a stable string key in the format
 * "<mcc>-<mnc>-<lac/tac>-<cid>" used for NamedLocation fingerprinting.
 * Returns null for unsupported or unavailable cell types.
 */
fun CellInfo.toCellIdString(): String? = when (this) {
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
            // CellInfoNr and CellIdentityNr are only available from API 29
            @Suppress("NewApi")
            val nrId = (this as? android.telephony.CellInfoNr)
                ?.cellIdentity as? android.telephony.CellIdentityNr
            nrId?.let { "${it.mccString}-${it.mncString}-${it.tac}-${it.nci}" }
                ?.takeIf { !it.contains("null") }
        } else null
    }
}
