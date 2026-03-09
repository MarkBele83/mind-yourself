package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import de.stroebele.mindyourself.ui.viewmodel.HydrationViewModel

private val AMOUNTS_ML = listOf(150, 300, 400, 500, 700, 1000)

/**
 * Hydration-Logging: Tippen fügt die Menge hinzu und bleibt auf dem Screen.
 * "Rückgängig" entfernt den letzten Eintrag.
 * Swipe-to-dismiss beendet den Screen (SwipeDismissableNavHost).
 */
@Composable
fun HydrationLogScreen(
    onDone: () -> Unit,
    viewModel: HydrationViewModel = hiltViewModel(),
) {
    val totalMl by viewModel.todayTotalMl.collectAsState()

    ScreenScaffold(timeText = { TimeText() }) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text(
                    text = "💧 $totalMl ml",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            items(AMOUNTS_ML) { amount ->
                Button(
                    onClick = { viewModel.log(amount) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("+ $amount ml")
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.undoLast() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("↩ Rückgängig")
                }
            }
        }
    }
}
