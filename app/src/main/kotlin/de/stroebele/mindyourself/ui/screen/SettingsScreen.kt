package de.stroebele.mindyourself.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import de.stroebele.mindyourself.ui.viewmodel.HcPermissionState
import de.stroebele.mindyourself.ui.viewmodel.SettingsViewModel
import de.stroebele.mindyourself.ui.viewmodel.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLocations: () -> Unit,
    onNavigateToVacation: () -> Unit,
    onNavigateToPortionSizes: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var stepGoalInput by remember { mutableStateOf(uiState.stepDailyGoal.toString()) }
    val context = LocalContext.current

    val hcPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        // Permissions returned directly from Health Connect — refresh state immediately.
        viewModel.checkHcPermissions()
    }

    // Backup export: file picker triggered when pendingBackupJson is set
    val backupExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val json = uiState.pendingBackupJson ?: return@let
            context.contentResolver.openOutputStream(it)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            }
        }
        viewModel.clearPendingBackupExport()
    }
    LaunchedEffect(uiState.pendingBackupJson) {
        if (uiState.pendingBackupJson != null) {
            backupExportLauncher.launch("mindyourself_backup.json")
        }
    }

    // Backup import: file picker
    val backupImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val json = context.contentResolver.openInputStream(it)
                ?.use { stream -> stream.bufferedReader().readText() }
            json?.let { viewModel.importBackupFromJson(it) }
        }
    }

    LaunchedEffect(uiState.backupMessage) {
        uiState.backupMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearBackupMessage()
        }
    }

    LaunchedEffect(uiState.stepDailyGoal) {
        stepGoalInput = uiState.stepDailyGoal.toString()
    }

    LaunchedEffect(uiState.syncState) {
        when (uiState.syncState) {
            SyncState.SUCCESS -> {
                snackbarHostState.showSnackbar("Sync erfolgreich")
                viewModel.resetSyncState()
            }
            SyncState.ERROR -> {
                snackbarHostState.showSnackbar("Sync fehlgeschlagen – Uhr in Reichweite?")
                viewModel.resetSyncState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Einstellungen") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Sync section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Watch-Synchronisation", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Überträgt alle Erinnerungsregeln auf die Uhr.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                        onClick = { viewModel.sync() },
                        enabled = uiState.syncState != SyncState.SYNCING,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (uiState.syncState == SyncState.SYNCING)
                                "Wird synchronisiert…"
                            else
                                "Zur Uhr synchronisieren"
                        )
                    }
                }
            }

            // Backup section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Backup", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Erinnerungen als JSON-Datei sichern oder wiederherstellen.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { backupImportLauncher.launch("application/json") },
                            modifier = Modifier.weight(1f),
                        ) { Text("Import") }
                        Button(
                            onClick = { viewModel.prepareBackupExport() },
                            modifier = Modifier.weight(1f),
                        ) { Text("Export") }
                    }
                }
            }
            if (uiState.hcPermissionState != HcPermissionState.NOT_AVAILABLE) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Health Connect", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when (uiState.hcPermissionState) {
                                HcPermissionState.GRANTED ->
                                    "Verbunden – Trinkmengen werden mit Health Connect synchronisiert."
                                HcPermissionState.MISSING ->
                                    "Nicht verbunden – Trinkmengen werden nicht mit anderen Apps geteilt."
                                else -> "Verbindungsstatus wird geprüft…"
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (uiState.hcPermissionState == HcPermissionState.MISSING) {
                            Button(
                                onClick = {
                                    hcPermissionLauncher.launch(viewModel.requiredHcPermissions)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Health Connect verbinden")
                            }
                        }
                    }
                }
            }

            // Step daily goal
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Schritte Tagesziel", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Wird auf der Uhr als Zielwert angezeigt und beim Sync übertragen.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = stepGoalInput,
                        onValueChange = { input ->
                            stepGoalInput = input
                            input.toIntOrNull()?.let { viewModel.saveStepGoal(it) }
                        },
                        label = { Text("Schritte pro Tag") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }

            // Vacation mode menu item
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToVacation)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Urlaubsmodus", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Zeiträume und Erinnerungsfilter für den Urlaub",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }

            // Locations menu item
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToLocations)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Orte", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "WLAN- und Mobilfunk-Fingerprints verwalten",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }

            // Portion sizes menu item
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToPortionSizes)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Trinkmengen", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Portionsgrößen für die Flüssigkeitserfassung",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }

            // Privacy section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Datenschutz", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Alle Daten werden ausschließlich lokal auf deinen Geräten gespeichert. " +
                        "Keine Cloud, keine Weitergabe an Dritte.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // App version
            val versionName = remember {
                context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionName ?: "–"
            }
            Text(
                text = "Version $versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
