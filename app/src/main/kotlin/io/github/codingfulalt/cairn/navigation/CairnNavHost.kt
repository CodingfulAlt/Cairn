package io.github.codingfulalt.cairn.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.codingfulalt.cairn.feature.detail.HabitDetailScreen
import io.github.codingfulalt.cairn.feature.editor.HabitEditorScreen
import io.github.codingfulalt.cairn.feature.habits.HabitsScreen
import io.github.codingfulalt.cairn.feature.insights.InsightsScreen
import io.github.codingfulalt.cairn.feature.settings.SettingsScreen
import io.github.codingfulalt.cairn.feature.today.TodayScreen

private const val FADE_MS = 220
private const val SLIDE_MS = 320

@Composable
fun CairnNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val openHabit: (Long) -> Unit = { navController.navigate(HabitDetailRoute(it)) }
    val newHabit: () -> Unit = { navController.navigate(HabitEditorRoute()) }
    val goBack: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = TodayRoute,
        modifier = modifier,
        enterTransition = { fadeIn(tween(FADE_MS)) },
        exitTransition = { fadeOut(tween(FADE_MS)) },
    ) {
        composable<TodayRoute> {
            TodayScreen(
                onOpenHabit = openHabit,
                onCreateHabit = newHabit,
                contentPadding = contentPadding,
            )
        }
        composable<HabitsRoute> {
            HabitsScreen(onOpenHabit = openHabit, contentPadding = contentPadding)
        }
        composable<InsightsRoute> {
            InsightsScreen(onOpenHabit = openHabit, contentPadding = contentPadding)
        }
        composable<SettingsRoute> {
            SettingsScreen(contentPadding = contentPadding)
        }
        composable<HabitDetailRoute>(
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(SLIDE_MS),
                )
            },
            exitTransition = { fadeOut(tween(FADE_MS)) },
            popEnterTransition = { fadeIn(tween(FADE_MS)) },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(SLIDE_MS),
                )
            },
        ) {
            HabitDetailScreen(
                onBack = goBack,
                onEdit = { navController.navigate(HabitEditorRoute(it)) },
            )
        }
        composable<HabitEditorRoute>(
            enterTransition = {
                slideInVertically(tween(SLIDE_MS)) { it / 3 } +
                    fadeIn(tween(SLIDE_MS))
            },
            popExitTransition = {
                slideOutVertically(tween(SLIDE_MS)) { it / 3 } +
                    fadeOut(tween(FADE_MS))
            },
        ) {
            HabitEditorScreen(onClose = goBack)
        }
    }
}
