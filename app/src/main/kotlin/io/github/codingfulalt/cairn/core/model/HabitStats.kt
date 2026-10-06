package io.github.codingfulalt.cairn.core.model

data class HabitStats(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completionRate: Float = 0f,
    val completedDays: Int = 0,
    val totalCheckIns: Int = 0,
)
