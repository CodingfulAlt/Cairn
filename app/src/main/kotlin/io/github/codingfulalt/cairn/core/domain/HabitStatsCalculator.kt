package io.github.codingfulalt.cairn.core.domain

import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitStats
import java.time.LocalDate

/*
 * Streak rules:
 *  - a day is done when count >= daily goal
 *  - days the habit isn't scheduled for never break a streak (but count if you did it anyway)
 *  - today never breaks a streak, the day isn't over yet
 */
object HabitStatsCalculator {
    private const val RATE_WINDOW_DAYS = 30L

    fun compute(
        habit: Habit,
        checkIns: List<CheckIn>,
        today: LocalDate,
    ): HabitStats {
        val counts =
            checkIns
                .filter { it.habitId == habit.id }
                .associate { it.date to it.count }
        return compute(habit, counts, today)
    }

    fun compute(
        habit: Habit,
        counts: Map<LocalDate, Int>,
        today: LocalDate,
    ): HabitStats {
        val start = startDate(habit, counts)
        if (start.isAfter(today)) return HabitStats()

        fun isDone(day: LocalDate) = (counts[day] ?: 0) >= habit.dailyGoal

        fun canSkip(day: LocalDate) = day == today || !habit.isScheduledOn(day)

        var current = 0
        var day = today
        while (!day.isBefore(start)) {
            if (isDone(day)) {
                current++
            } else if (!canSkip(day)) {
                break
            }
            day = day.minusDays(1)
        }

        var best = 0
        var run = 0
        day = start
        while (!day.isAfter(today)) {
            if (isDone(day)) {
                run++
                best = maxOf(best, run)
            } else if (!canSkip(day)) {
                run = 0
            }
            day = day.plusDays(1)
        }

        var scheduled = 0
        var completed = 0
        day = maxOf(start, today.minusDays(RATE_WINDOW_DAYS - 1))
        while (!day.isAfter(today)) {
            val done = isDone(day)
            // an unfinished today shouldn't drag the rate down
            if (habit.isScheduledOn(day) && (day != today || done)) {
                scheduled++
                if (done) completed++
            }
            day = day.plusDays(1)
        }

        return HabitStats(
            currentStreak = current,
            bestStreak = best,
            completionRate = if (scheduled == 0) 0f else completed.toFloat() / scheduled,
            completedDays = counts.values.count { it >= habit.dailyGoal },
            totalCheckIns = counts.values.sum(),
        )
    }

    private fun startDate(
        habit: Habit,
        counts: Map<LocalDate, Int>,
    ): LocalDate {
        val firstCheckIn = counts.filterValues { it > 0 }.keys.minOrNull()
        return if (firstCheckIn != null && firstCheckIn.isBefore(habit.createdOn)) {
            firstCheckIn
        } else {
            habit.createdOn
        }
    }
}
