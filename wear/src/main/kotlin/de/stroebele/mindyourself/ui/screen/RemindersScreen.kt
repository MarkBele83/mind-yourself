package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.SupplementConfig
import de.stroebele.mindyourself.ui.viewmodel.RemindersViewModel
import java.time.DayOfWeek

/**
 * Format active days as German abbreviations with range shortening.
 * Examples: "Mo-So" (all days), "Mo-Fr" (weekdays), "Sa-So" (weekend), "MoDiMi" (custom)
 */
private fun formatActiveDays(days: Set<DayOfWeek>): String {
    if (days.isEmpty()) return ""

    val germanDayNames = mapOf(
        DayOfWeek.MONDAY to "Mo",
        DayOfWeek.TUESDAY to "Di",
        DayOfWeek.WEDNESDAY to "Mi",
        DayOfWeek.THURSDAY to "Do",
        DayOfWeek.FRIDAY to "Fr",
        DayOfWeek.SATURDAY to "Sa",
        DayOfWeek.SUNDAY to "So"
    )

    val sorted = days.sortedBy { it.value }
    val dayValues = sorted.map { it.value }

    // Check if all days
    if (sorted.size == 7) return "Mo-So"
    // Check for weekdays (Mon-Fri, 1-5)
    if (dayValues == listOf(1, 2, 3, 4, 5)) return "Mo-Fr"
    // Check for weekend (Sat-Sun, 6-7)
    if (dayValues == listOf(6, 7)) return "Sa-So"

    // Otherwise list individual days
    return sorted.joinToString("") { germanDayNames[it] ?: "" }
}

/**
 * Read-only list of all synced reminder configs.
 * Allows the user to verify that the phone → watch sync was successful.
 * Shows location filters and vacation status.
 */
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()
    val listState = rememberScalingLazyListState()

    ScreenScaffold(
        timeText = { TimeText() },
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
        ) {
            item {
                Text("Erinnerungen", style = MaterialTheme.typography.titleMedium)
            }

            if (items.isEmpty()) {
                item {
                    Text(
                        "Keine Erinnerungen vorhanden.\nBitte Sync in der App auslösen.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            items(items) { item ->
                val config = item.config
                val typeEmoji = when (config.type) {
                    ReminderType.HYDRATION -> "💧"
                    ReminderType.SUPPLEMENT -> "💊"
                    ReminderType.MOVEMENT -> "🏃"
                    ReminderType.SEDENTARY -> "⏸"
                    ReminderType.SCREEN_BREAK -> "👁"
                }
                val status = if (config.enabled) "" else " (inaktiv)"
                val days = formatActiveDays(config.activeDays)

                val vacationStatus = if (!config.activeInVacation) " ⏸" else ""
                val locationInfo = if (item.locationNames.isNotEmpty())
                    "\n📍 ${item.locationNames.joinToString(", ")}"
                else
                    ""
                val timeInfo = when (val tc = config.typeConfig) {
                    is SupplementConfig -> tc.scheduledTime.toString()
                    else -> "${config.activeFrom}–${config.activeUntil}"
                }

                Text(
                    "$typeEmoji ${config.label}$status\n$days  $timeInfo$vacationStatus$locationInfo",
                    style = if (config.enabled)
                        MaterialTheme.typography.bodySmall
                    else
                        MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
