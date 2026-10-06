package io.github.codingfulalt.cairn.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.codingfulalt.cairn.feature.onboarding.OnboardingScreen
import io.github.codingfulalt.cairn.navigation.CairnNavHost
import io.github.codingfulalt.cairn.navigation.HabitEditorRoute
import io.github.codingfulalt.cairn.navigation.navigateToTopLevel
import io.github.codingfulalt.cairn.navigation.topLevelDestination
import io.github.codingfulalt.cairn.ui.components.BOTTOM_BAR_CLEARANCE
import io.github.codingfulalt.cairn.ui.components.CairnBottomBar
import io.github.codingfulalt.cairn.ui.components.CairnNavRail

private val RAIL_BREAKPOINT = 600.dp

@Composable
fun CairnApp(showOnboarding: Boolean) {
    if (showOnboarding) {
        OnboardingScreen(Modifier.fillMaxSize())
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination.topLevelDestination()
    val newHabit = { navController.navigate(HabitEditorRoute()) }
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (maxWidth >= RAIL_BREAKPOINT) {
            Row(Modifier.fillMaxSize()) {
                CairnNavRail(
                    current = current,
                    onSelect = navController::navigateToTopLevel,
                    onAdd = newHabit,
                )
                CairnNavHost(
                    navController = navController,
                    contentPadding = PaddingValues(bottom = navBarInset + 16.dp),
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            CairnNavHost(
                navController = navController,
                contentPadding = PaddingValues(bottom = navBarInset + BOTTOM_BAR_CLEARANCE),
                modifier = Modifier.fillMaxSize(),
            )
            AnimatedVisibility(
                visible = current != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                CairnBottomBar(
                    current = current,
                    onSelect = navController::navigateToTopLevel,
                    onAdd = newHabit,
                )
            }
        }
    }
}
