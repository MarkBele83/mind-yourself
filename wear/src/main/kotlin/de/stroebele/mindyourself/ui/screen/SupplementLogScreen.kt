package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import de.stroebele.mindyourself.ui.viewmodel.SupplementViewModel

/**
 * Confirmation screen for logging a supplement intake.
 * Nutzerzentrierung: Ein-Tap Bestätigung, Swipe-to-dismiss zum Abbrechen.
 */
@Composable
fun SupplementLogScreen(
    supplementName: String,
    onDone: () -> Unit,
    viewModel: SupplementViewModel = hiltViewModel(),
) {
    ScreenScaffold(timeText = { TimeText() }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(supplementName)
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                viewModel.log(supplementName)
                onDone()
            }) {
                Text("Genommen ✓")
            }
        }
    }
}
