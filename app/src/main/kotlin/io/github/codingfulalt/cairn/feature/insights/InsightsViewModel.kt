package io.github.codingfulalt.cairn.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.domain.DayProgress
import io.github.codingfulalt.cairn.core.domain.HabitStatsCalculator
import io.github.codingfulalt.cairn.core.domain.ProgressCalculator
import io.github.codingfulalt.cairn.core.domain.toIndex
import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class StreakLeader(
    val habit: Habit,
    val streak: Int,
)

data class InsightsUiState(
    val loading: Boolean = true,
    val hasHabits: Boolean = false,
    val perfectDays: Int = 0,
    val completionRate: Float = 0f,
    val topStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val today: LocalDate? = null,
    val lastSevenDays: List<DayProgress> = emptyList(),
    val heatmap: List<Float?> = emptyList(),
    val leaders: List<StreakLeader> = emptyList(),
)

@HiltViewModel
class InsightsViewModel
    @Inject
    constructor(
        habitRepository: HabitRepository,
        private val clock: Clock,
    ) : ViewModel() {
        val uiState: StateFlow<InsightsUiState> =
            combine(
                habitRepository.observeHabits(),
                habitRepository.observeCheckIns(),
                ::buildState,
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

        private fun buildState(
            habits: List<Habit>,
            checkIns: List<CheckIn>,
        ): InsightsUiState {
            val today = LocalDate.now(clock)
            val active = habits.filterNot { it.archived }
            if (active.isEmpty()) return InsightsUiState(loading = false, today = today)

            val activeIds = active.map { it.id }.toSet()
            val relevant = checkIns.filter { it.habitId in activeIds }
            val index = relevant.toIndex()
            val firstDay =
                active.minOf { it.createdOn }.let { created ->
                    minOf(
                        created,
                        index.keys.minOrNull() ?: created,
                    )
                }
            val history = ProgressCalculator.range(active, index, firstDay, today)

            // an unfinished today would make the rate look worse than it is
            val window =
                history.takeLast(RATE_WINDOW_DAYS).filter {
                    it.date != today ||
                        it.isPerfect
                }
            val scheduled = window.sumOf { it.scheduled }
            val completed = window.sumOf { it.completed }

            val byHabit = relevant.groupBy { it.habitId }
            val leaders =
                active
                    .map { habit ->
                        val counts = byHabit[habit.id].orEmpty().associate { it.date to it.count }
                        StreakLeader(
                            habit,
                            HabitStatsCalculator.compute(habit, counts, today).currentStreak,
                        )
                    }.filter { it.streak > 0 }
                    .sortedByDescending { it.streak }

            val gridStart =
                today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(
                    HEATMAP_WEEKS - 1L,
                )
            val heatmap =
                List(HEATMAP_WEEKS * 7) { offset ->
                    val day = gridStart.plusDays(offset.toLong())
                    if (day.isAfter(
                            today,
                        )
                    ) {
                        null
                    } else {
                        ProgressCalculator.dayProgress(active, index, day).ratio
                    }
                }

            return InsightsUiState(
                loading = false,
                hasHabits = true,
                perfectDays = history.count { it.isPerfect },
                completionRate = if (scheduled == 0) 0f else completed.toFloat() / scheduled,
                topStreak = leaders.firstOrNull()?.streak ?: 0,
                totalCheckIns = relevant.sumOf { it.count },
                today = today,
                lastSevenDays = history.takeLast(7),
                heatmap = heatmap,
                leaders = leaders.take(MAX_LEADERS),
            )
        }

        companion object {
            const val HEATMAP_WEEKS = 18
            private const val RATE_WINDOW_DAYS = 30
            private const val MAX_LEADERS = 5
        }
    }
