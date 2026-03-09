package de.stroebele.mindyourself

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import de.stroebele.mindyourself.service.PassiveMonitoringManager
import de.stroebele.mindyourself.ui.navigation.MindYourselfNavGraph
import de.stroebele.mindyourself.ui.permission.PermissionRationaleScreen
import de.stroebele.mindyourself.ui.theme.MindYourselfTheme
import de.stroebele.mindyourself.worker.ReminderEvaluationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Permissions are requested one at a time in logical order.
 * Already-granted permissions are skipped on every launch.
 * Core = BODY_SENSORS + ACTIVITY_RECOGNITION (required for reminders).
 * POST_NOTIFICATIONS is requested separately; refusal does not block the app.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var passiveMonitoringManager: PassiveMonitoringManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Which permission is currently being presented to the user
    private var pendingPermission by mutableStateOf<SensorPermission?>(null)
    // True once all core permissions are granted
    private var coreGranted by mutableStateOf(false)

    private val singlePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val current = pendingPermission ?: return@registerForActivityResult
        if (!granted && current.required) {
            // Stay on rationale for this permission — user must grant it
            return@registerForActivityResult
        }
        advancePermissionFlow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coreGranted = corePermissionsGranted()

        setContent {
            MindYourselfTheme {
                val pending = pendingPermission
                when {
                    pending != null -> PermissionRationaleScreen(
                        permission = pending,
                        onGrant = { singlePermissionLauncher.launch(pending.manifest) },
                        onSkip = if (!pending.required) ({ advancePermissionFlow() }) else null,
                    )
                    coreGranted -> MindYourselfNavGraph()
                    else -> {
                        // First launch: start the permission flow
                        startPermissionFlow()
                        PermissionRationaleScreen(
                            permission = permissionQueue().first(),
                            onGrant = {
                                val p = permissionQueue().first()
                                pendingPermission = p
                                singlePermissionLauncher.launch(p.manifest)
                            },
                            onSkip = null,
                        )
                    }
                }
            }
        }

        if (coreGranted) onCorePermissionsGranted()
    }

    private fun startPermissionFlow() {
        val queue = permissionQueue()
        pendingPermission = queue.firstOrNull()
    }

    private fun advancePermissionFlow() {
        val queue = permissionQueue()
        val currentIndex = queue.indexOfFirst { it.manifest == pendingPermission?.manifest }
        val next = queue.getOrNull(currentIndex + 1)

        pendingPermission = next
        if (next == null) {
            coreGranted = corePermissionsGranted()
            if (coreGranted) onCorePermissionsGranted()
        }
    }

    /** Returns only permissions not yet granted, in order. */
    private fun permissionQueue(): List<SensorPermission> =
        allPermissions().filter {
            checkSelfPermission(it.manifest) != PackageManager.PERMISSION_GRANTED
        }

    private fun corePermissionsGranted(): Boolean =
        allPermissions().filter { it.required }.all {
            checkSelfPermission(it.manifest) == PackageManager.PERMISSION_GRANTED
        }

    private fun allPermissions(): List<SensorPermission> = buildList {
        add(SensorPermission(
            manifest = Manifest.permission.ACTIVITY_RECOGNITION,
            title = "Aktivitätserkennung",
            rationale = "Wird benötigt, um Bewegungs- und Sitz-Pause-Erinnerungen auszulösen.",
            required = true,
        ))
        if (Build.VERSION.SDK_INT >= 36) {
            add(SensorPermission(
                manifest = "android.permission.READ_HEART_RATE",
                title = "Herzrate",
                rationale = "Ermöglicht das Cachen von Herzratedaten für spätere HRV-Analyse.",
                required = false,
            ))
        } else {
            add(SensorPermission(
                manifest = Manifest.permission.BODY_SENSORS,
                title = "Körpersensoren",
                rationale = "Wird benötigt, um Herzratedaten im Hintergrund zu empfangen.",
                required = false,
            ))
        }
        if (Build.VERSION.SDK_INT >= 33) {
            add(SensorPermission(
                manifest = Manifest.permission.POST_NOTIFICATIONS,
                title = "Benachrichtigungen",
                rationale = "Damit mindYourself Erinnerungen als Benachrichtigungen senden kann.",
                required = false,
            ))
        }
    }

    private fun onCorePermissionsGranted() {
        scope.launch { passiveMonitoringManager.register() }
        ReminderEvaluationWorker.schedule(WorkManager.getInstance(this))
    }
}

data class SensorPermission(
    val manifest: String,
    val title: String,
    val rationale: String,
    val required: Boolean,
)
