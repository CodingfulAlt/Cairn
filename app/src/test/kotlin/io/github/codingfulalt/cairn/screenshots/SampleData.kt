package io.github.codingfulalt.cairn.screenshots

import io.github.codingfulalt.cairn.core.domain.DayProgress
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import io.github.codingfulalt.cairn.core.model.HabitStats
import io.github.codingfulalt.cairn.feature.detail.HabitDetailUiState
import io.github.codingfulalt.cairn.feature.habits.DayMark
import io.github.codingfulalt.cairn.feature.habits.HabitSummary
import io.github.codingfulalt.cairn.feature.habits.HabitsUiState
import io.github.codingfulalt.cairn.feature.insights.InsightsUiState
import io.github.codingfulalt.cairn.feature.insights.StreakLeader
import io.github.codingfulalt.cairn.feature.today.TodayHabit
import io.github.codingfulalt.cairn.feature.today.TodayUiState
import io.github.codingfulalt.cairn.feature.today.WeekDay
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

internal object SampleData {
    // a Thursday
    private val today = LocalDate.of(2026, 9, 10)
    private val start = today.minusDays(90)

    private val water =
        Habit(
            1,
            "Drink water",
            icon = HabitIcon.Water,
            color = HabitColor.Sky,
            dailyGoal = 6,
            createdOn = start,
        )
    private val read =
        Habit(
            id = 2,
            name = "Read 10 pages",
            description = "Fiction counts too.",
            icon = HabitIcon.Book,
            color = HabitColor.Lavender,
            reminder = LocalTime.of(21, 30),
            createdOn = start,
        )
    private val run =
        Habit(
            3,
            "Morning run",
            icon = HabitIcon.Run,
            color = HabitColor.Moss,
            schedule = Habit.WEEKDAYS,
            createdOn = start,
        )
    private val meditate =
        Habit(4, "Meditate", icon = HabitIcon.Meditate, color = HabitColor.Mint, createdOn = start)
    private val journal =
        Habit(
            5,
            "Write 3 lines",
            icon = HabitIcon.Write,
            color = HabitColor.Sand,
            createdOn = start,
        )
    private val stretch =
        Habit(6, "Stretch", icon = HabitIcon.Workout, color = HabitColor.Peach, createdOn = start)
    private val guitar =
        Habit(
            7,
            "Guitar practice",
            icon = HabitIcon.Music,
            color = HabitColor.Coral,
            archived = true,
            createdOn = start,
        )

    val todayState =
        TodayUiState(
            loading = false,
            userName = "Alex",
            hour = 9,
            today = today,
            week =
                listOf(1f, 0.83f, 1f, 0.5f, 0f, 0f, 0f).mapIndexed { index, ratio ->
                    val date = today.minusDays(3).plusDays(index.toLong())
                    WeekDay(date, ratio, isToday = date == today, isFuture = date.isAfter(today))
                },
            pending =
                listOf(
                    TodayHabit(water, 4, 12),
                    TodayHabit(run, 0, 5),
                    TodayHabit(journal, 0, 0),
                ),
            done =
                listOf(
                    TodayHabit(read, 1, 23),
                    TodayHabit(meditate, 1, 9),
                    TodayHabit(stretch, 1, 3),
                ),
            progress = DayProgress(today, scheduled = 6, completed = 3),
            hasHabits = true,
        )

    val detailState =
        HabitDetailUiState(
            loading = false,
            habit = read,
            todayCount = 1,
            stats =
                HabitStats(
                    currentStreak = 23,
                    bestStreak = 31,
                    completionRate = 0.93f,
                    totalCheckIns = 142,
                ),
            heatmap = heatmap(weeks = 20, seed = 7, partial = false),
        )

    val insightsState =
        InsightsUiState(
            loading = false,
            hasHabits = true,
            perfectDays = 41,
            completionRate = 0.86f,
            topStreak = 23,
            totalCheckIns = 1184,
            today = today,
            lastSevenDays =
                listOf(6, 5, 4, 6, 5, 6, 3).mapIndexed { index, done ->
                    DayProgress(today.minusDays(6L - index), scheduled = 6, completed = done)
                },
            heatmap = heatmap(weeks = 18, seed = 11, partial = true),
            leaders =
                listOf(
                    StreakLeader(read, 23),
                    StreakLeader(water, 12),
                    StreakLeader(meditate, 9),
                    StreakLeader(run, 5),
                    StreakLeader(stretch, 3),
                ),
        )

    val habitsState =
        HabitsUiState(
            loading = false,
            active =
                listOf(
                    summary(read, 23, 31, 0.93f, "DDDDDDD"),
                    summary(water, 12, 19, 0.81f, "DDMDDDP"),
                    summary(meditate, 9, 14, 0.77f, "DDDDDDD"),
                    summary(run, 5, 11, 0.7f, "DOODDDP"),
                    summary(stretch, 3, 8, 0.62f, "DMMMDDD"),
                    summary(journal, 0, 6, 0.48f, "DMDMMMP"),
                ),
            archived = listOf(summary(guitar, 0, 17, 0.4f, "OOOOOOO")),
        )

    private fun summary(
        habit: Habit,
        current: Int,
        best: Int,
        rate: Float,
        marks: String,
    ) = HabitSummary(
        habit = habit,
        stats = HabitStats(currentStreak = current, bestStreak = best, completionRate = rate),
        lastSevenDays =
            marks.map {
                when (it) {
                    'D' -> DayMark.Done
                    'M' -> DayMark.Missed
                    'P' -> DayMark.Pending
                    else -> DayMark.Off
                }
            },
    )

    // older weeks are sparser, like someone who got better at it over time
    private fun heatmap(
        weeks: Int,
        seed: Int,
        partial: Boolean,
    ): List<Float?> {
        val random = Random(seed)
        val todayIndex = (weeks - 1) * 7 + 3
        return List(weeks * 7) { index ->
            if (index > todayIndex) return@List null
            val hit = random.nextFloat() < 0.4f + 0.55f * index / todayIndex
            when {
                !hit -> 0f
                partial -> 0.3f + random.nextFloat() * 0.7f
                else -> 1f
            }
        }
    }
}
