package io.github.codingfulalt.cairn.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.domain.HabitStatsCalculator
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

enum class DayMark { Done, Missed, Pending, Off }

data class HabitSummary(
    val habit: Habit,
    val stats: HabitStats,
    val lastSevenDays: List<DayMark>,
)

data class HabitsUiState(
    val loading: Boolean = true,
    val active: List<HabitSummary> = emptyList(),
    val archived: List<HabitSummary> = emptyList(),
)

@HiltViewModel
class HabitsViewModel
    @Inject
    constructor(
        habitRepository: HabitRepository,
        private val clock: Clock,
    ) : ViewModel() {
        val uiState: StateFlow<HabitsUiState> =
            combine(
                habitRepository.observeHabits(),
                habitRepository.observeCheckIns(),
            ) { habits, checkIns ->
                val today = LocalDate.now(clock)
                val byHabit = checkIns.groupBy { it.habitId }
                val summaries =
                    habits.map { habit ->
                        val counts = byHabit[habit.id].orEmpty().associate { it.date to it.count }
                        HabitSummary(
                            habit = habit,
                            stats = HabitStatsCalculator.compute(habit, counts, today),
                            lastSevenDays =
                                (6 downTo 0).map { back ->
                                    val day = today.minusDays(back.toLong())
                                    mark(habit, counts[day] ?: 0, day, today)
                                },
                        )
                    }
                HabitsUiState(
                    loading = false,
                    active = summaries.filterNot { it.habit.archived },
                    archived = summaries.filter { it.habit.archived },
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState())

        private fun mark(
            habit: Habit,
            count: Int,
            day: LocalDate,
            today: LocalDate,
        ): DayMark =
            when {
                count >= habit.dailyGoal -> DayMark.Done
                !habit.isActiveOn(day, count) -> DayMark.Off
                day == today -> DayMark.Pending
                else -> DayMark.Missed
            }
    }
