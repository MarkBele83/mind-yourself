package de.stroebele.mindyourself.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText

/**
 * Einstellungen für Wear OS.
 * Menü mit Erinnerungen und App-Einstellungen.
 */
@Composable
fun SettingsScreen(
    onNavigateToReminders: () -> Unit,
) {
    val context = LocalContext.current

    ScreenScaffold(
        timeText = { TimeText() },
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Text("Einstellungen", style = MaterialTheme.typography.titleMedium)
            }

            item {
                Button(
                    onClick = onNavigateToReminders,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Erinnerungen")
                }
            }

            item {
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("App-Einstellungen")
                }
            }

            item {
                val versionName = remember {
                    context.packageManager
                        .getPackageInfo(context.packageName, 0)
                        .versionName ?: "–"
                }
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
