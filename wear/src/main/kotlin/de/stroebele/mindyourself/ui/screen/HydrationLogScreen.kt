package de.stroebele.mindyourself.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import de.stroebele.mindyourself.ui.viewmodel.HydrationViewModel

private val FALLBACK_AMOUNTS = listOf(150, 200, 300, 500, 750)

/**
 * Flüssigkeitserfassung für Wear OS:
 * - Oben: Bisheriger Tagesgesamtstand
 * - Mitte: Vertikales Carousel mit − [Portionsgröße] +
 * - Unten: Gesamtstand mit Erfassung
 */
@Composable
fun HydrationLogScreen(
    onDone: () -> Unit,
    viewModel: HydrationViewModel = hiltViewModel(),
) {
    val totalMl by viewModel.todayTotalMl.collectAsState()
    val portionSizes by viewModel.portionSizes.collectAsState()
    val amounts = portionSizes.map { it.amountMl }.takeIf { it.isNotEmpty() } ?: FALLBACK_AMOUNTS

    val listState = rememberScalingLazyListState()
    val selectedIndex by remember {
        derivedStateOf { listState.centerItemIndex.coerceIn(amounts.indices) }
    }
    val selectedAmount = amounts.getOrNull(selectedIndex) ?: amounts.first()
    var accumulatedMl by remember { mutableIntStateOf(0) }

    // Beim Screen-Exit: speichere erfasste Menge, wenn > 0
    DisposableEffect(Unit) {
        onDispose {
            if (accumulatedMl > 0) {
                Log.d("HydrationLogScreen", "Auto-saving $accumulatedMl ml on screen exit")
                viewModel.log(accumulatedMl)
            }
        }
    }

    ScreenScaffold(timeText = { TimeText() }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Oben: Bisheriger Tagesgesamtstand
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "💧 $totalMl ml",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Mitte: Carousel mit − [Portionsgröße] +
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // − Button (links, 25% Höhe)
                Button(
                    onClick = {
                        if (accumulatedMl >= selectedAmount) {
                            accumulatedMl -= selectedAmount
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight(0.25f)
                        .padding(start = 4.dp),
                ) {
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }

                // Hero Carousel (Mitte)
                ScalingLazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 48.dp),
                ) {
                    items(amounts) { amount ->
                        val isCenter = amount == selectedAmount
                        Text(
                            text = "$amount ml",
                            style = if (isCenter)
                                MaterialTheme.typography.displaySmall
                            else
                                MaterialTheme.typography.titleMedium,
                            color = if (isCenter)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // + Button (rechts, 25% Höhe) - addiert zur Erfassung
                Button(
                    onClick = { accumulatedMl += selectedAmount },
                    modifier = Modifier
                        .fillMaxHeight(0.25f)
                        .padding(end = 4.dp),
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Unten: Gesamtvolumen mit Erfassung
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "💧 ${totalMl + accumulatedMl} ml",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
