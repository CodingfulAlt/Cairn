package io.github.codingfulalt.cairn.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.domain.HabitStatsCalculator
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitStats
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HabitDetailUiState(
    val loading: Boolean = true,
    val habit: Habit? = null,
    val todayCount: Int = 0,
    val stats: HabitStats = HabitStats(),
    val heatmap: List<Float?> = emptyList(),
    val deleted: Boolean = false,
)

@HiltViewModel
class HabitDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val habitRepository: HabitRepository,
        private val reminderScheduler: ReminderScheduler,
        private val clock: Clock,
    ) : ViewModel() {
        private val habitId: Long = checkNotNull(savedStateHandle["habitId"])
        private val deleted = MutableStateFlow(false)

        val uiState: StateFlow<HabitDetailUiState> =
            combine(
                habitRepository.observeHabit(habitId),
                habitRepository.observeCheckIns(habitId),
                deleted,
            ) { habit, checkIns, isDeleted ->
                val today = LocalDate.now(clock)
                val counts = checkIns.associate { it.date to it.count }
                HabitDetailUiState(
                    loading = false,
                    habit = habit,
                    todayCount = counts[today] ?: 0,
                    stats =
                        habit?.let { HabitStatsCalculator.compute(it, counts, today) }
                            ?: HabitStats(),
                    heatmap = habit?.let { heatmap(it, counts, today) }.orEmpty(),
                    deleted = isDeleted,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitDetailUiState())

        fun checkIn() = adjust(1)

        fun undoCheckIn() = adjust(-1)

        fun toggleArchived() {
            val habit = uiState.value.habit ?: return
            viewModelScope.launch {
                val archived = !habit.archived
                habitRepository.setArchived(habit.id, archived)
                if (archived) {
                    reminderScheduler.cancelHabit(habit.id)
                } else {
                    reminderScheduler.scheduleHabit(habit.copy(archived = false))
                }
            }
        }

        fun delete() {
            viewModelScope.launch {
                reminderScheduler.cancelHabit(habitId)
                habitRepository.deleteHabit(habitId)
                deleted.value = true
            }
        }

        private fun adjust(delta: Int) {
            val today = LocalDate.now(clock)
            viewModelScope.launch { habitRepository.adjustCheckIn(habitId, today, delta) }
        }

        private fun heatmap(
            habit: Habit,
            counts: Map<LocalDate, Int>,
            today: LocalDate,
        ): List<Float?> {
            val start =
                today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(
                    HEATMAP_WEEKS - 1L,
                )
            return List(HEATMAP_WEEKS * 7) { offset ->
                val day = start.plusDays(offset.toLong())
                if (day.isAfter(
                        today,
                    )
                ) {
                    null
                } else {
                    ((counts[day] ?: 0) / habit.dailyGoal.toFloat()).coerceAtMost(1f)
                }
            }
        }

        companion object {
            const val HEATMAP_WEEKS = 20
        }
    }
