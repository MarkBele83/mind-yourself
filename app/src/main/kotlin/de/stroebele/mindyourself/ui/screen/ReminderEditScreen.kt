package de.stroebele.mindyourself.ui.screen

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.MovementConfig
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.ScreenBreakConfig
import de.stroebele.mindyourself.domain.model.SedentaryConfig
import de.stroebele.mindyourself.domain.model.SupplementConfig
import de.stroebele.mindyourself.domain.model.SupplementForm
import de.stroebele.mindyourself.domain.model.SupplementItem
import de.stroebele.mindyourself.ui.viewmodel.ReminderEditViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditScreen(
    reminderType: ReminderType?,
    onSaved: () -> Unit,
    viewModel: ReminderEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(reminderType) {
        reminderType?.let { viewModel.initTemplate(it) }
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    val config = uiState.config ?: return

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(if (config.id == 0L) "Neue Erinnerung" else "Bearbeiten") }
            )
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
            // Label
            OutlinedTextField(
                value = config.label,
                onValueChange = { viewModel.update(config.copy(label = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.labelError != null,
                supportingText = uiState.labelError?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                },
            )

            // Active days
            Text("Aktive Tage")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DayOfWeek.entries.forEach { day ->
                    val selected = day in config.activeDays
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val days = if (day in config.activeDays)
                                config.activeDays - day
                            else
                                config.activeDays + day
                            viewModel.update(config.copy(activeDays = days))
                        },
                        label = {
                            Text(
                                day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selected = selected,
                            enabled = true,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }

            // Active window (not shown for supplements — time is part of the type config)
            if (config.type != ReminderType.SUPPLEMENT) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField(
                        label = "Von",
                        time = config.activeFrom,
                        onTimeChange = { viewModel.update(config.copy(activeFrom = it)) },
                        modifier = Modifier.weight(1f),
                    )
                    TimeField(
                        label = "Bis",
                        time = config.activeUntil,
                        onTimeChange = { viewModel.update(config.copy(activeUntil = it)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Type-specific fields
            TypeConfigFields(config = config, onUpdate = { viewModel.update(it) })

            HorizontalDivider()

            // Location filter
            LocationFilterSection(
                config = config,
                namedLocations = uiState.namedLocations,
                onFilterEnabled = { viewModel.setLocationFilterEnabled(it) },
                onToggleLocation = { viewModel.toggleAllowedLocation(it) },
                onStrictModeChanged = { viewModel.setStrictMode(it) },
            )

            // Vacation mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Im Urlaub aktiv")
                    Text(
                        "Erinnerung auch im Urlaubsmodus anzeigen",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = config.activeInVacation,
                    onCheckedChange = { viewModel.update(config.copy(activeInVacation = it)) },
                )
            }

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Speichern")
            }
        }
    }
}

@Composable
private fun LocationFilterSection(
    config: ReminderConfig,
    namedLocations: List<NamedLocation>,
    onFilterEnabled: (Boolean) -> Unit,
    onToggleLocation: (Long) -> Unit,
    onStrictModeChanged: (Boolean) -> Unit,
) {
    val filter = config.locationFilter
    val filterEnabled = filter != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Ortsabhängig")
            Text(
                "Nur an bestimmten Orten erinnern",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            checked = filterEnabled,
            onCheckedChange = onFilterEnabled,
        )
    }

    if (filterEnabled && filter != null) {
        if (namedLocations.isEmpty()) {
            Text(
                "Keine Orte konfiguriert. Lege zuerst Orte in den Einstellungen an.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text("Aktive Orte", style = MaterialTheme.typography.labelMedium)
            namedLocations.forEach { location ->
                val selected = location.id in filter.allowedLocationIds
                FilterChip(
                    selected = selected,
                    onClick = { onToggleLocation(location.id) },
                    label = { Text(location.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        selected = selected,
                        enabled = true,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Unbekannter Ort: überspringen")
                Text(
                    "Wenn kein Ort erkannt wird, nicht erinnern",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = filter.strictMode,
                onCheckedChange = onStrictModeChanged,
            )
        }
    }
}

@Composable
private fun TypeConfigFields(
    config: ReminderConfig,
    onUpdate: (ReminderConfig) -> Unit,
) {
    when (val tc = config.typeConfig) {
        is MovementConfig -> {
            NumberField(
                label = "Schritte-Schwellwert",
                value = tc.stepThreshold,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(stepThreshold = it))) },
            )
            NumberField(
                label = "Schrittzähler-Fenster (Minuten)",
                value = tc.windowMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(windowMinutes = it))) },
            )
            NumberField(
                label = "Wiederholungsintervall (Minuten)",
                value = tc.repeatIntervalMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(repeatIntervalMinutes = it))) },
            )
        }
        is SedentaryConfig -> {
            NumberField(
                label = "Inaktivität bis Alarm (Minuten)",
                value = tc.inactiveThresholdMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(inactiveThresholdMinutes = it))) },
            )
            NumberField(
                label = "Wiederholungsintervall (Minuten)",
                value = tc.repeatIntervalMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(repeatIntervalMinutes = it))) },
            )
        }
        is HydrationConfig -> {
            NumberField(
                label = "Zielwert im Zeitraum (ml)",
                value = tc.reminderGoalMl,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(reminderGoalMl = it))) },
            )
            NumberField(
                label = "Wiederholungsintervall (Minuten)",
                value = tc.repeatIntervalMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(repeatIntervalMinutes = it))) },
            )
            NumberField(
                label = "Keine-Aufnahme-Fenster (Minuten)",
                value = tc.noLogWindowMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(noLogWindowMinutes = it))) },
            )
        }
        is SupplementConfig -> {
            TimeField(
                label = "Erinnerungszeit",
                time = tc.scheduledTime,
                onTimeChange = { onUpdate(config.copy(typeConfig = tc.copy(scheduledTime = it))) },
            )
            HorizontalDivider()
            Text("Supplemente", style = MaterialTheme.typography.labelMedium)
            tc.items.forEachIndexed { index, item ->
                SupplementItemRow(
                    item = item,
                    onItemChange = { updated ->
                        onUpdate(config.copy(typeConfig = tc.copy(
                            items = tc.items.toMutableList().also { it[index] = updated }
                        )))
                    },
                    onRemove = if (tc.items.size > 1) {
                        { onUpdate(config.copy(typeConfig = tc.copy(items = tc.items - item))) }
                    } else null,
                )
            }
            TextButton(
                onClick = {
                    onUpdate(config.copy(typeConfig = tc.copy(
                        items = tc.items + SupplementItem(name = "", amount = 1, form = SupplementForm.CAPSULE)
                    )))
                },
            ) { Text("+ Supplement hinzufügen") }
            HorizontalDivider()
            NumberField(
                label = "Snooze-Dauer (Minuten)",
                value = tc.snoozeDurationMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(snoozeDurationMinutes = it))) },
            )
        }
        is ScreenBreakConfig -> {
            NumberField(
                label = "Intervall (Minuten)",
                value = tc.intervalMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(intervalMinutes = it))) },
            )
            NumberField(
                label = "Pausendauer (Minuten)",
                value = tc.breakDurationMinutes,
                onValueChange = { onUpdate(config.copy(typeConfig = tc.copy(breakDurationMinutes = it))) },
            )
        }
    }
}

@Composable
private fun SupplementItemRow(
    item: SupplementItem,
    onItemChange: (SupplementItem) -> Unit,
    onRemove: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = item.name,
                onValueChange = { onItemChange(item.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Entfernen")
                }
            }
        }
        NumberField(
            label = "Menge",
            value = item.amount,
            onValueChange = { onItemChange(item.copy(amount = it.coerceAtLeast(1))) },
            modifier = Modifier.fillMaxWidth(0.4f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SupplementForm.entries.forEach { form ->
                val selected = item.form == form
                FilterChip(
                    selected = selected,
                    onClick = { onItemChange(item.copy(form = form)) },
                    label = { Text(form.label()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        selected = selected,
                        enabled = true,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
        }
    }
}

private fun SupplementForm.label(): String = when (this) {
    SupplementForm.CAPSULE -> "Kapsel"
    SupplementForm.PILL -> "Tablette"
    SupplementForm.DROP -> "Tropfen"
    SupplementForm.GUM -> "Gummie"
}

@Composable
private fun NumberField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toIntOrNull() ?: value) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    label: String,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = "%02d:%02d".format(time.hour, time.minute),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "Zeit wählen")
            }
        },
        modifier = modifier,
        singleLine = true,
    )

    if (showPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(LocalTime.of(pickerState.hour, pickerState.minute))
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Abbrechen") }
            },
            text = { TimePicker(state = pickerState) },
        )
    }
}
