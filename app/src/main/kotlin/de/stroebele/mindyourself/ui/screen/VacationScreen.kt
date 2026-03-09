package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.stroebele.mindyourself.ui.viewmodel.VacationViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacationScreen(
    onNavigateBack: () -> Unit,
    viewModel: VacationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.overlapError) {
        if (uiState.overlapError) {
            snackbarHostState.showSnackbar("Zeitraum überschneidet sich mit einem bestehenden")
            viewModel.clearOverlapError()
        }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // 4-step dialog state machine: from_date → from_time → until_date → until_time
    var dialogStep by remember { mutableStateOf<String?>(null) }
    var pendingFromDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingFrom by remember { mutableStateOf<LocalDateTime?>(null) }
    var pendingUntilDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Urlaubsmodus") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
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
            // Status card — read-only toggle
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Status", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Im Urlaubsmodus werden nur Erinnerungen angezeigt, die explizit als \"Im Urlaub aktiv\" markiert sind.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (uiState.isCurrentlyInVacation) "Urlaubsmodus aktiv"
                            else "Urlaubsmodus inaktiv"
                        )
                        Switch(
                            checked = uiState.isCurrentlyInVacation,
                            onCheckedChange = null, // read-only
                        )
                    }
                }
            }

            // Periods list
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Urlaubszeiträume", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { dialogStep = "from_date" }) {
                            Icon(Icons.Default.Add, contentDescription = "Zeitraum hinzufügen")
                        }
                    }

                    if (uiState.settings.periods.isEmpty()) {
                        Text(
                            "Noch keine Zeiträume definiert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        uiState.settings.periods.forEachIndexed { index, period ->
                            if (index > 0) HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Von: ${period.from.format(dateFormatter)}  ${period.from.format(timeFormatter)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        "Bis: ${period.until.format(dateFormatter)}  ${period.until.format(timeFormatter)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                IconButton(onClick = { viewModel.removePeriod(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Löschen",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog state machine
    when (dialogStep) {
        "from_date" -> {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { dialogStep = null },
                confirmButton = {
                    TextButton(onClick = {
                        val ms = datePickerState.selectedDateMillis
                        if (ms != null) {
                            pendingFromDate = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                            dialogStep = "from_time"
                        } else dialogStep = null
                    }) { Text("Weiter") }
                },
                dismissButton = { TextButton(onClick = { dialogStep = null }) { Text("Abbrechen") } },
            ) { DatePicker(state = datePickerState) }
        }
        "from_time" -> {
            val timePickerState = rememberTimePickerState(0, 0, is24Hour = true)
            AlertDialog(
                onDismissRequest = { dialogStep = null },
                confirmButton = {
                    TextButton(onClick = {
                        val date = pendingFromDate ?: LocalDate.now()
                        pendingFrom = LocalDateTime.of(date, LocalTime.of(timePickerState.hour, timePickerState.minute))
                        dialogStep = "until_date"
                    }) { Text("Weiter") }
                },
                dismissButton = { TextButton(onClick = { dialogStep = null }) { Text("Abbrechen") } },
                text = { TimePicker(state = timePickerState) },
            )
        }
        "until_date" -> {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { dialogStep = null },
                confirmButton = {
                    TextButton(onClick = {
                        val ms = datePickerState.selectedDateMillis
                        if (ms != null) {
                            pendingUntilDate = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                            dialogStep = "until_time"
                        } else dialogStep = null
                    }) { Text("Weiter") }
                },
                dismissButton = { TextButton(onClick = { dialogStep = null }) { Text("Abbrechen") } },
            ) { DatePicker(state = datePickerState) }
        }
        "until_time" -> {
            val timePickerState = rememberTimePickerState(23, 59, is24Hour = true)
            AlertDialog(
                onDismissRequest = { dialogStep = null },
                confirmButton = {
                    TextButton(onClick = {
                        val from = pendingFrom
                        val untilDate = pendingUntilDate ?: LocalDate.now()
                        val until = LocalDateTime.of(untilDate, LocalTime.of(timePickerState.hour, timePickerState.minute))
                        if (from != null) viewModel.addPeriod(from, until)
                        pendingFromDate = null; pendingFrom = null; pendingUntilDate = null
                        dialogStep = null
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { dialogStep = null }) { Text("Abbrechen") } },
                text = { TimePicker(state = timePickerState) },
            )
        }
    }
}
