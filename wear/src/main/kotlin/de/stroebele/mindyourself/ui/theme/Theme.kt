package de.stroebele.mindyourself.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

/**
 * mindYourself Wear OS theme.
 * Black background is enforced via MaterialTheme defaults (Wear OS Quality requirement).
 */
@Composable
fun MindYourselfTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
