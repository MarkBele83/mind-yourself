package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.ui.viewmodel.LocationListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationListScreen(
    onAddLocation: () -> Unit,
    onEditLocation: (Long) -> Unit,
    viewModel: LocationListViewModel = hiltViewModel(),
) {
    val locations by viewModel.locations.collectAsState()
    var deleteCandidate by remember { mutableStateOf<NamedLocation?>(null) }

    deleteCandidate?.let { location ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Ort löschen") },
            text = { Text("\"${location.name}\" wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(location.id)
                    deleteCandidate = null
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Abbrechen") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orte") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLocation) {
                Icon(Icons.Default.Add, contentDescription = "Ort hinzufügen")
            }
        },
    ) { padding ->
        if (locations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Noch keine Orte angelegt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Tippe auf + um einen Ort hinzuzufügen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(locations, key = { it.id }) { location ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditLocation(location.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(location.name, style = MaterialTheme.typography.bodyLarge)
                            val hint = buildList {
                                if (location.wifiSsid != null) add("WLAN: ${location.wifiSsid}")
                                if (location.cellIds.isNotEmpty()) add("${location.cellIds.size} Funkzelle(n)")
                            }.joinToString(" · ")
                            if (hint.isNotEmpty()) {
                                Text(
                                    hint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(onClick = { deleteCandidate = location }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Löschen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
