package de.stroebele.mindyourself.ui.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import de.stroebele.mindyourself.ui.screen.HomeScreen
import de.stroebele.mindyourself.ui.screen.HydrationLogScreen
import de.stroebele.mindyourself.ui.screen.RemindersScreen
import de.stroebele.mindyourself.ui.screen.SettingsScreen
import de.stroebele.mindyourself.ui.screen.SupplementLogScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object HydrationLog : Screen("hydration_log")
    data object SupplementLog : Screen("supplement_log/{name}") {
        fun route(name: String) = "supplement_log/$name"
    }
    data object Settings : Screen("settings")
    data object Reminders : Screen("reminders")
}

/**
 * Swipe-to-dismiss navigation — mandatory Wear OS Quality requirement.
 * Every screen except Home can be dismissed by swiping right.
 */
@Composable
fun MindYourselfNavGraph() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onLogHydration = { navController.navigate(Screen.HydrationLog.route) },
                onLogSupplement = { name -> navController.navigate(Screen.SupplementLog.route(name)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.HydrationLog.route) {
            HydrationLogScreen(onDone = { navController.popBackStack() })
        }
        composable(Screen.SupplementLog.route) {
            val name = it.arguments?.getString("name") ?: return@composable
            SupplementLogScreen(supplementName = name, onDone = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
            )
        }
        composable(Screen.Reminders.route) {
            RemindersScreen()
        }
    }
}
