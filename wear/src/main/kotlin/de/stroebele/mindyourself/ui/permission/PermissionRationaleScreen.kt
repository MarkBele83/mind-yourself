package de.stroebele.mindyourself.ui.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import de.stroebele.mindyourself.SensorPermission

/**
 * One screen per permission — shown only if not yet granted.
 * [onSkip] is non-null only for optional permissions.
 */
@Composable
fun PermissionRationaleScreen(
    permission: SensorPermission,
    onGrant: () -> Unit,
    onSkip: (() -> Unit)?,
) {
    ScreenScaffold(timeText = { TimeText() }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = permission.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = permission.rationale,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onGrant) {
                Text("Erlauben")
            }
            if (onSkip != null) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                ) {
                    Text("Überspringen")
                }
            }
        }
    }
}
