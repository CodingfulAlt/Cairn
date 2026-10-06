package io.github.codingfulalt.cairn.core.domain

import io.github.codingfulalt.cairn.core.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HabitStatsCalculatorTest {
    // Thursday
    private val today = LocalDate.of(2026, 9, 10)

    private fun habit(
        createdDaysAgo: Long = 30,
        goal: Int = 1,
        schedule: Set<java.time.DayOfWeek> = Habit.EVERY_DAY,
        on: LocalDate = today,
    ) = Habit(
        id = 1,
        name = "Read",
        dailyGoal = goal,
        schedule = schedule,
        createdOn = on.minusDays(createdDaysAgo),
    )

    private fun doneOn(vararg daysAgo: Long) = daysAgo.associate { today.minusDays(it) to 1 }

    @Test
    fun emptyHistory_givesZeroes() {
        val stats = HabitStatsCalculator.compute(habit(), emptyMap(), today)

        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.bestStreak)
        assertEquals(0, stats.totalCheckIns)
    }

    @Test
    fun unfinishedToday_doesNotBreakStreak() {
        val stats = HabitStatsCalculator.compute(habit(), doneOn(1, 2, 3), today)

        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
    }

    @Test
    fun finishingToday_extendsStreak() {
        val stats = HabitStatsCalculator.compute(habit(), doneOn(0, 1), today)

        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun missedScheduledDay_breaksStreak() {
        val stats = HabitStatsCalculator.compute(habit(), doneOn(1, 3, 4), today)

        assertEquals(1, stats.currentStreak)
        assertEquals(2, stats.bestStreak)
    }

    @Test
    fun weekend_doesNotBreakWeekdayStreak() {
        val monday = LocalDate.of(2026, 9, 14)
        val counts = mapOf(LocalDate.of(2026, 9, 11) to 1, monday to 1)

        val stats =
            HabitStatsCalculator.compute(
                habit(schedule = Habit.WEEKDAYS, on = monday),
                counts,
                monday,
            )

        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun partialCount_isNotADoneDay() {
        val counts = mapOf(today.minusDays(1) to 2)

        val stats = HabitStatsCalculator.compute(habit(goal = 3), counts, today)

        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.completedDays)
        assertEquals(2, stats.totalCheckIns)
    }

    @Test
    fun completionRate_skipsUnfinishedToday() {
        val stats = HabitStatsCalculator.compute(habit(createdDaysAgo = 3), doneOn(1, 3), today)

        assertEquals(2f / 3f, stats.completionRate, 0.001f)
    }
}
