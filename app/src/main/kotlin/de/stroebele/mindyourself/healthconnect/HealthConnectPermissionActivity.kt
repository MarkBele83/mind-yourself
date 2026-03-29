package de.stroebele.mindyourself.healthconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import dagger.hilt.android.AndroidEntryPoint
import de.stroebele.mindyourself.ui.theme.MindYourselfTheme
import javax.inject.Inject

/**
 * Handles Health Connect permission requests and also serves as the
 * permissions rationale screen required by Google Play policy.
 */
@AndroidEntryPoint
class HealthConnectPermissionActivity : ComponentActivity() {

    @Inject lateinit var healthConnectManager: HealthConnectManager

    private val requestPermissions =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            val allGranted = granted.containsAll(healthConnectManager.requiredPermissions)
            setResult(if (allGranted) RESULT_OK else RESULT_CANCELED)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if this is a rationale display or a permission request
        if (intent.action == "androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE") {
            showRationaleScreen()
        } else {
            requestPermissions.launch(healthConnectManager.requiredPermissions)
        }
    }

    private fun showRationaleScreen() {
        setContent {
            MindYourselfTheme {
                Scaffold { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            "Health Connect",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            "mindYourself möchte deine Trinkmengen in Health Connect speichern " +
                            "und dort erfasste Mengen anderer Apps auf deine Uhr übertragen.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "Deine Daten verlassen niemals dein Gerät. Health Connect speichert " +
                            "alle Daten lokal.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Button(
                            onClick = { requestPermissions.launch(healthConnectManager.requiredPermissions) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Berechtigungen erteilen")
                        }
                        Button(
                            onClick = { finish() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Abbrechen")
                        }
                    }
                }
            }
        }
    }
}
