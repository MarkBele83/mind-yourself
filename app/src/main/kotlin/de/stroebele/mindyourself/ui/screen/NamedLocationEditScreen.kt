package de.stroebele.mindyourself.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import de.stroebele.mindyourself.ui.viewmodel.NamedLocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamedLocationEditScreen(
    onSaved: () -> Unit,
    viewModel: NamedLocationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val wifiPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.captureCurrentWifi() }

    val cellPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> if (permissions.values.all { it }) viewModel.captureCurrentCellIds() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    val location = uiState.location
    val isEdit = location.id != 0L

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Ort löschen") },
            text = { Text("\"${location.name}\" wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete() }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEdit) "Ort bearbeiten" else "Neuer Ort") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Name
            OutlinedTextField(
                value = location.name,
                onValueChange = { viewModel.update(location.copy(name = it)) },
                label = { Text("Name (z.B. Zuhause, Büro)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            )

            // WLAN
            Text("WLAN-Fingerprint", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = location.wifiSsid ?: "",
                onValueChange = {
                    viewModel.update(location.copy(wifiSsid = it.takeIf { s -> s.isNotBlank() }))
                },
                label = { Text("WLAN-Name (SSID)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Wird automatisch erkannt") },
            )
            OutlinedButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.captureCurrentWifi()
                    } else {
                        wifiPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Aktuelles WLAN übernehmen")
            }

            // Cell IDs
            Text("Mobilfunk-Fallback", style = MaterialTheme.typography.labelLarge)
            Text(
                "Wird verwendet, wenn kein WLAN verfügbar ist. " +
                "Genauigkeit: ca. 100–300 m (Stadt), 1–5 km (Land).",
                style = MaterialTheme.typography.bodySmall,
            )
            if (location.cellIds.isNotEmpty()) {
                Text(
                    "${location.cellIds.size} Funkzelle(n) gespeichert",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val perms = arrayOf(
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                        if (perms.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
                            viewModel.captureCurrentCellIds()
                        } else {
                            cellPermissionLauncher.launch(perms)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Funkzellen erfassen")
                }
                if (location.cellIds.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.update(location.copy(cellIds = emptyList())) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Zurücksetzen")
                    }
                }
            }

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Speichern")
            }

            if (isEdit) {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Ort löschen")
                }
            }
        }
    }
}
