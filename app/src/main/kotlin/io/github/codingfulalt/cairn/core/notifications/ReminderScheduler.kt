package io.github.codingfulalt.cairn.core.notifications

import io.github.codingfulalt.cairn.core.model.Habit
import java.time.LocalTime

interface ReminderScheduler {
    fun scheduleHabit(habit: Habit)

    fun cancelHabit(habitId: Long)

    fun scheduleDailySummary(time: LocalTime)

    fun cancelDailySummary()

    suspend fun rescheduleAll()
}
