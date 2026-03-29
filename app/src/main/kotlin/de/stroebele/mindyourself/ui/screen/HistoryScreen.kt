package de.stroebele.mindyourself.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.stroebele.mindyourself.domain.model.HydrationLog
import de.stroebele.mindyourself.domain.model.SupplementLog
import de.stroebele.mindyourself.ui.viewmodel.HistoryViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Verlauf") }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Trinken") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Supplements") },
                )
            }

            when (selectedTab) {
                0 -> HydrationHistory(
                    logs = uiState.hydrationLogs,
                    totalMl = uiState.todayHydrationMl,
                )
                1 -> SupplementHistory(logs = uiState.supplementLogs)
            }
        }
    }
}

@Composable
private fun HydrationHistory(logs: List<HydrationLog>, totalMl: Int) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Heute: $totalMl ml",
                modifier = Modifier.padding(16.dp),
            )
        }
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(logs) { log ->
                ListItem(
                    headlineContent = { Text("${log.amountMl} ml") },
                    supportingContent = {
                        Text(
                            log.timestamp
                                .atZone(ZoneId.systemDefault())
                                .format(timeFormatter)
                        )
                    },
                    leadingContent = { Text("💧") },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SupplementHistory(logs: List<SupplementLog>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(logs) { log ->
            ListItem(
                headlineContent = { Text(log.supplementName) },
                supportingContent = {
                    Text(
                        log.takenAt
                            .atZone(ZoneId.systemDefault())
                            .format(timeFormatter)
                    )
                },
                leadingContent = { Text("💊") },
            )
            HorizontalDivider()
        }
    }
}
