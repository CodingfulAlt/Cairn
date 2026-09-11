package io.github.codingfulalt.cairn.core.model

import java.time.LocalTime

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val userName: String = "",
    val onboardingCompleted: Boolean = false,
    val dailySummaryEnabled: Boolean = false,
    val dailySummaryTime: LocalTime = DEFAULT_SUMMARY_TIME,
) {
    companion object {
        val DEFAULT_SUMMARY_TIME: LocalTime = LocalTime.of(20, 0)
    }
}

enum class ThemeMode { System, Light, Dark }
