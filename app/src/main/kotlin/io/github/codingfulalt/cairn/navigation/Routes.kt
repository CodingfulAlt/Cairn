package io.github.codingfulalt.cairn.navigation

import kotlinx.serialization.Serializable

@Serializable
data object TodayRoute

@Serializable
data object HabitsRoute

@Serializable
data object InsightsRoute

@Serializable
data object SettingsRoute

@Serializable
data class HabitDetailRoute(
    val habitId: Long,
)

/** habitId 0 opens an empty editor for a new habit. */
@Serializable
data class HabitEditorRoute(
    val habitId: Long = 0L,
)
