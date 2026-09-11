package io.github.codingfulalt.cairn.core.domain

import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import java.time.LocalDate

data class DayProgress(
    val date: LocalDate,
    val scheduled: Int,
    val completed: Int,
) {
    val ratio: Float get() = if (scheduled == 0) 0f else completed.toFloat() / scheduled
    val isPerfect: Boolean get() = scheduled > 0 && completed >= scheduled
}

// date -> (habit id -> count)
typealias CheckInIndex = Map<LocalDate, Map<Long, Int>>

fun List<CheckIn>.toIndex(): CheckInIndex =
    groupBy { it.date }.mapValues { (_, dayCheckIns) ->
        dayCheckIns.associate { it.habitId to it.count }
    }

object ProgressCalculator {
    fun dayProgress(
        habits: List<Habit>,
        index: CheckInIndex,
        date: LocalDate,
    ): DayProgress {
        val counts = index[date].orEmpty()
        var scheduled = 0
        var completed = 0
        for (habit in habits) {
            val count = counts[habit.id] ?: 0
            if (!habit.isActiveOn(date, count)) continue
            scheduled++
            if (count >= habit.dailyGoal) completed++
        }
        return DayProgress(date, scheduled, completed)
    }

    fun range(
        habits: List<Habit>,
        index: CheckInIndex,
        from: LocalDate,
        to: LocalDate,
    ): List<DayProgress> =
        generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(to) }
            .map { dayProgress(habits, index, it) }
            .toList()
}
