package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import de.stroebele.mindyourself.ui.viewmodel.HomeViewModel

/**
 * Home screen — the primary watch UI.
 *
 * Wear OS Quality requirements met:
 * - TimeText displayed at the top (mandatory for home screens)
 * - ScalingLazyColumn with rotary scroll support
 * - Black background via MaterialTheme
 * - Swipe-to-dismiss handled at NavGraph level
 */
@Composable
fun HomeScreen(
    onLogHydration: () -> Unit,
    onLogSupplement: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState: ScalingLazyListState = rememberScalingLazyListState()

    AppScaffold {
        ScreenScaffold(
            timeText = { TimeText() }, // Wear OS Quality: time always visible
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
            ) {
                item {
                    Text(text = "mindYourself")
                }

                // Hydration quick-log with progress
                item {
                    Button(onClick = onLogHydration) {
                        Text("💧 ${uiState.todayHydrationMl} / ${uiState.hydrationGoalMl} ml (${uiState.hydrationPercent}%)")
                    }
                }

                // Supplement summary (read-only) + quick-log buttons
                item {
                    Text("💊 ${uiState.supplementsTakenToday} von ${uiState.supplementsTotalToday}")
                }
                items(uiState.supplementNames) { name ->
                    Button(onClick = { onLogSupplement(name) }) {
                        Text("💊 $name")
                    }
                }

                // Today's step count with goal and percentage
                item {
                    Text(text = "👟 ${uiState.todaySteps} / ${uiState.stepDailyGoal} (${uiState.stepsPercent}%)")
                }

                // Settings menu
                item {
                    Button(onClick = onNavigateToSettings) {
                        Text("Einstellungen")
                    }
                }
            }
        }
    }
}
