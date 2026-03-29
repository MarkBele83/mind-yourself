package de.stroebele.mindyourself.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.ui.screen.HistoryScreen
import de.stroebele.mindyourself.ui.screen.HydrationPortionSizeScreen
import de.stroebele.mindyourself.ui.screen.LocationListScreen
import de.stroebele.mindyourself.ui.screen.NamedLocationEditScreen
import de.stroebele.mindyourself.ui.screen.ReminderEditScreen
import de.stroebele.mindyourself.ui.screen.ReminderListScreen
import de.stroebele.mindyourself.ui.screen.ReminderTypePickerScreen
import de.stroebele.mindyourself.ui.screen.SettingsScreen
import de.stroebele.mindyourself.ui.screen.VacationScreen

private sealed class Screen(val route: String, val label: String) {
    data object ReminderList : Screen("reminders", "Erinnerungen")
    data object History : Screen("history", "Verlauf")
    data object Settings : Screen("settings", "Einstellungen")
    data object TypePicker : Screen("reminders/new", "Typ wählen")
    data object ReminderEdit : Screen("reminders/{reminderId}?type={type}", "Bearbeiten")
    data object Vacation : Screen("vacation", "Urlaubsmodus")
    data object LocationList : Screen("locations", "Orte")
    data object LocationNew : Screen("locations/new", "Neuer Ort")
    data object LocationEdit : Screen("locations/{locationId}", "Ort bearbeiten")
    data object PortionSizes : Screen("portion-sizes", "Trinkmengen")
}

@Composable
fun MindYourselfNavGraph() {
    val navController = rememberNavController()
    val topLevelRoutes = listOf(Screen.ReminderList, Screen.History, Screen.Settings)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = topLevelRoutes.any { it.route == currentDestination?.route }
            if (showBottomBar) {
                NavigationBar {
                    topLevelRoutes.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon(), contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.ReminderList.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ReminderList.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.ReminderList.route) {
                ReminderListScreen(
                    onAddReminder = { navController.navigate(Screen.TypePicker.route) },
                    onEditReminder = { id -> navController.navigate("reminders/$id") },
                )
            }
            composable(Screen.TypePicker.route) {
                ReminderTypePickerScreen(
                    onTypePicked = { type ->
                        navController.navigate("reminders/0?type=${type.name}")
                    },
                )
            }
            composable(
                route = "reminders/{reminderId}?type={type}",
                arguments = listOf(
                    navArgument("reminderId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) { backStackEntry ->
                val typeArg = backStackEntry.arguments?.getString("type")
                val reminderType = typeArg?.let { runCatching { ReminderType.valueOf(it) }.getOrNull() }
                ReminderEditScreen(
                    reminderType = reminderType,
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToLocations = { navController.navigate(Screen.LocationList.route) },
                    onNavigateToVacation = { navController.navigate(Screen.Vacation.route) },
                    onNavigateToPortionSizes = { navController.navigate(Screen.PortionSizes.route) },
                )
            }
            composable(Screen.Vacation.route) {
                VacationScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.LocationList.route) {
                LocationListScreen(
                    onAddLocation = { navController.navigate(Screen.LocationNew.route) },
                    onEditLocation = { id -> navController.navigate("locations/$id") },
                )
            }
            composable(Screen.LocationNew.route) {
                NamedLocationEditScreen(
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(Screen.PortionSizes.route) {
                HydrationPortionSizeScreen()
            }
            composable(
                route = "locations/{locationId}",
                arguments = listOf(
                    navArgument("locationId") { type = NavType.LongType },
                ),
            ) {
                NamedLocationEditScreen(
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun Screen.icon() = when (this) {
    Screen.ReminderList -> Icons.Default.Notifications
    Screen.History -> Icons.Default.DateRange
    Screen.Settings -> Icons.Default.Settings
    else -> Icons.Default.Notifications
}
