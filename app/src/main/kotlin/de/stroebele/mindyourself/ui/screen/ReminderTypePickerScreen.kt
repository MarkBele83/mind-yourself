package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.stroebele.mindyourself.domain.model.ReminderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTypePickerScreen(
    onTypePicked: (ReminderType) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Typ wählen") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ReminderType.entries.filter { it != ReminderType.SEDENTARY && it != ReminderType.SCREEN_BREAK }) { type ->
                Card(
                    onClick = { onTypePicked(type) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ListItem(
                        leadingContent = { Text(type.icon()) },
                        headlineContent = { Text(type.displayName()) },
                        supportingContent = { Text(type.description()) },
                    )
                }
            }
        }
    }
}

private fun ReminderType.icon(): String = when (this) {
    ReminderType.MOVEMENT -> "🏃"
    ReminderType.SEDENTARY -> "🪑"
    ReminderType.HYDRATION -> "💧"
    ReminderType.SUPPLEMENT -> "💊"
    ReminderType.SCREEN_BREAK -> "👁"
}

private fun ReminderType.displayName(): String = when (this) {
    ReminderType.MOVEMENT -> "Bewegung"
    ReminderType.SEDENTARY -> "Sitz-Pause"
    ReminderType.HYDRATION -> "Trinken"
    ReminderType.SUPPLEMENT -> "Supplement"
    ReminderType.SCREEN_BREAK -> "Bildschirmpause"
}

private fun ReminderType.description(): String = when (this) {
    ReminderType.MOVEMENT -> "Erinnert wenn zu wenig Schritte in einem Zeitfenster"
    ReminderType.SEDENTARY -> "Erinnert nach längerer Inaktivität"
    ReminderType.HYDRATION -> "Erinnert regelmäßig ans Trinken"
    ReminderType.SUPPLEMENT -> "Erinnert zu konfigurierbaren Zeiten"
    ReminderType.SCREEN_BREAK -> "Erinnert in festem Intervall an Bildschirmpausen"
}
