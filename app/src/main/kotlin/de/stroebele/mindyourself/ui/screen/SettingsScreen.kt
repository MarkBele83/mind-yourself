package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.stroebele.mindyourself.ui.viewmodel.SettingsViewModel
import de.stroebele.mindyourself.ui.viewmodel.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLocations: () -> Unit,
    onNavigateToVacation: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
        }
    }
}
