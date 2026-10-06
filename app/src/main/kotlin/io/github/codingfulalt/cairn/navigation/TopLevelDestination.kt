package io.github.codingfulalt.cairn.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import io.github.codingfulalt.cairn.R

enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    @param:StringRes val label: Int,
) {
    Today(TodayRoute, Icons.Outlined.WbSunny, Icons.Rounded.WbSunny, R.string.nav_today),
    Habits(HabitsRoute, Icons.Outlined.Layers, Icons.Rounded.Layers, R.string.nav_habits),
    Insights(InsightsRoute, Icons.Outlined.Insights, Icons.Rounded.Insights, R.string.nav_insights),
    Settings(SettingsRoute, Icons.Outlined.Settings, Icons.Rounded.Settings, R.string.nav_settings),
}

fun NavDestination?.topLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { top ->
        this?.hierarchy?.any { it.hasRoute(top.route::class) } == true
    }

fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
