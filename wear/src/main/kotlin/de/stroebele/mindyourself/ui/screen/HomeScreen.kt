package de.stroebele.mindyourself.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState: ScalingLazyListState = rememberScalingLazyListState()
    val context = LocalContext.current

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

                // Hydration quick-log
                item {
                    Button(onClick = onLogHydration) {
                        Text("💧 ${uiState.todayHydrationMl} ml")
                    }
                }

                // Supplement quick-log buttons (one per configured supplement)
                items(uiState.supplementNames) { name ->
                    Button(onClick = { onLogSupplement(name) }) {
                        Text("💊 $name")
                    }
                }

                // Today's step count (read-only, from PassiveListenerService)
                item {
                    Text(text = "👟 ${uiState.todaySteps} Schritte")
                }

                // App settings (permissions etc.) → system settings
                item {
                    Button(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }) {
                        Text("Einstellungen")
                    }
                }
            }
        }
    }
}
