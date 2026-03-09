package de.stroebele.mindyourself.ui.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import de.stroebele.mindyourself.ui.screen.HomeScreen
import de.stroebele.mindyourself.ui.screen.HydrationLogScreen
import de.stroebele.mindyourself.ui.screen.SupplementLogScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object HydrationLog : Screen("hydration_log")
    data object SupplementLog : Screen("supplement_log/{name}") {
        fun route(name: String) = "supplement_log/$name"
    }
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
            )
        }
        composable(Screen.HydrationLog.route) {
            HydrationLogScreen(onDone = { navController.popBackStack() })
        }
        composable(Screen.SupplementLog.route) {
            val name = it.arguments?.getString("name") ?: return@composable
            SupplementLogScreen(supplementName = name, onDone = { navController.popBackStack() })
        }
    }
}
