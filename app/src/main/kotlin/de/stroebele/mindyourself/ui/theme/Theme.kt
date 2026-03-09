package de.stroebele.mindyourself.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D66),
    onPrimaryContainer = Color(0xFFB8EAFF),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E5),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E5),
)

@Composable
fun MindYourselfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content,
    )
}
