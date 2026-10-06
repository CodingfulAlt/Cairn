package io.github.codingfulalt.cairn.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.domain.DayProgress
import io.github.codingfulalt.cairn.core.domain.HabitStatsCalculator
import io.github.codingfulalt.cairn.core.domain.ProgressCalculator
import io.github.codingfulalt.cairn.core.domain.toIndex
import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

data class TodayHabit(
    val habit: Habit,
    val count: Int,
    val streak: Int,
) {
    val isDone: Boolean get() = count >= habit.dailyGoal
}

data class WeekDay(
    val date: LocalDate,
    val ratio: Float,
    val isToday: Boolean,
    val isFuture: Boolean,
)

data class TodayUiState(
    val loading: Boolean = true,
    val userName: String = "",
    val hour: Int = 12,
    val today: LocalDate,
    val selectedDate: LocalDate = today,
    val week: List<WeekDay> = emptyList(),
    val pending: List<TodayHabit> = emptyList(),
    val done: List<TodayHabit> = emptyList(),
    val progress: DayProgress = DayProgress(today, 0, 0),
    val hasHabits: Boolean = false,
) {
    val isViewingToday: Boolean get() = selectedDate == today
    val canGoToNextWeek: Boolean get() = week.lastOrNull()?.date?.isBefore(today) == true
}

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        preferencesRepository: UserPreferencesRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val selectedDate = MutableStateFlow(today())

        val uiState: StateFlow<TodayUiState> =
            combine(
                habitRepository.observeHabits(),
                habitRepository.observeCheckIns(),
                preferencesRepository.preferences,
                selectedDate,
                ::buildState,
            ).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                TodayUiState(today = today()),
            )

        fun selectDate(date: LocalDate) {
            if (!date.isAfter(today())) selectedDate.value = date
        }

        fun previousWeek() = selectedDate.update { it.minusWeeks(1) }

        fun nextWeek() = selectedDate.update { minOf(it.plusWeeks(1), today()) }

        fun jumpToToday() {
            selectedDate.value = today()
        }

        fun checkIn(habitId: Long) = adjust(habitId, 1)

        fun undoCheckIn(habitId: Long) = adjust(habitId, -1)

        private fun adjust(
            habitId: Long,
            delta: Int,
        ) {
            val date = selectedDate.value
            viewModelScope.launch { habitRepository.adjustCheckIn(habitId, date, delta) }
        }

        private fun today(): LocalDate = LocalDate.now(clock)

        private fun buildState(
            habits: List<Habit>,
            checkIns: List<CheckIn>,
            preferences: UserPreferences,
            selected: LocalDate,
        ): TodayUiState {
            val today = today()
            val active = habits.filterNot { it.archived }
            val index = checkIns.toIndex()
            val countsByHabit = checkIns.groupBy { it.habitId }
            val dayCounts = index[selected].orEmpty()

            val items =
                active
                    .filter { it.isActiveOn(selected, dayCounts[it.id] ?: 0) }
                    .map { habit ->
                        val history =
                            countsByHabit[habit.id].orEmpty().associate {
                                it.date to
                                    it.count
                            }
                        TodayHabit(
                            habit = habit,
                            count = dayCounts[habit.id] ?: 0,
                            streak =
                                HabitStatsCalculator
                                    .compute(
                                        habit,
                                        history,
                                        today,
                                    ).currentStreak,
                        )
                    }

            val firstDay = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            val weekStart = selected.with(TemporalAdjusters.previousOrSame(firstDay))
            val week =
                List(7) { offset ->
                    val date = weekStart.plusDays(offset.toLong())
                    WeekDay(
                        date = date,
                        ratio = ProgressCalculator.dayProgress(active, index, date).ratio,
                        isToday = date == today,
                        isFuture = date.isAfter(today),
                    )
                }

            return TodayUiState(
                loading = false,
                userName = preferences.userName,
                hour = LocalTime.now(clock).hour,
                today = today,
                selectedDate = selected,
                week = week,
                pending = items.filterNot { it.isDone },
                done = items.filter { it.isDone },
                progress = ProgressCalculator.dayProgress(active, index, selected),
                hasHabits = active.isNotEmpty(),
            )
        }
    }
