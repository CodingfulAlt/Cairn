package io.github.codingfulalt.cairn.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class HabitEditorUiState(
    val loading: Boolean = false,
    val isNew: Boolean = true,
    val name: String = "",
    val description: String = "",
    val icon: HabitIcon = HabitIcon.Sparkle,
    val color: HabitColor = HabitColor.Moss,
    val dailyGoal: Int = 1,
    val schedule: Set<DayOfWeek> = Habit.EVERY_DAY,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = DEFAULT_REMINDER,
    val showErrors: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
) {
    val nameError: Boolean get() = name.isBlank()
    val scheduleError: Boolean get() = schedule.isEmpty()
    val canSave: Boolean get() = !nameError && !scheduleError

    companion object {
        val DEFAULT_REMINDER: LocalTime = LocalTime.of(9, 0)
    }
}

@HiltViewModel
class HabitEditorViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val habitRepository: HabitRepository,
        private val reminderScheduler: ReminderScheduler,
        private val clock: Clock,
    ) : ViewModel() {
        private val habitId: Long = savedStateHandle["habitId"] ?: 0L
        private var original: Habit? = null

        private val state =
            MutableStateFlow(
                HabitEditorUiState(
                    loading = habitId != 0L,
                    isNew =
                        habitId == 0L,
                ),
            )
        val uiState: StateFlow<HabitEditorUiState> = state.asStateFlow()

        init {
            if (habitId != 0L) {
                viewModelScope.launch {
                    val habit = habitRepository.getHabit(habitId)
                    original = habit
                    state.update { current ->
                        if (habit == null) {
                            current.copy(loading = false, isNew = true)
                        } else {
                            current.copy(
                                loading = false,
                                name = habit.name,
                                description = habit.description,
                                icon = habit.icon,
                                color = habit.color,
                                dailyGoal = habit.dailyGoal,
                                schedule = habit.schedule,
                                reminderEnabled = habit.reminder != null,
                                reminderTime = habit.reminder ?: current.reminderTime,
                            )
                        }
                    }
                }
            }
        }

        fun onNameChange(name: String) =
            state.update { it.copy(name = name.take(Habit.MAX_NAME_LENGTH)) }

        fun onDescriptionChange(text: String) =
            state.update {
                it.copy(description = text.take(Habit.MAX_NOTE_LENGTH))
            }

        fun onIconChange(icon: HabitIcon) = state.update { it.copy(icon = icon) }

        fun onColorChange(color: HabitColor) = state.update { it.copy(color = color) }

        fun changeGoal(delta: Int) =
            state.update {
                it.copy(
                    dailyGoal = (it.dailyGoal + delta).coerceIn(1, Habit.MAX_DAILY_GOAL),
                )
            }

        fun toggleDay(day: DayOfWeek) =
            state.update {
                it.copy(
                    schedule =
                        if (day in
                            it.schedule
                        ) {
                            it.schedule - day
                        } else {
                            it.schedule + day
                        },
                )
            }

        fun setSchedule(days: Set<DayOfWeek>) = state.update { it.copy(schedule = days) }

        fun setReminderEnabled(enabled: Boolean) =
            state.update { it.copy(reminderEnabled = enabled) }

        fun setReminderTime(time: LocalTime) =
            state.update {
                it.copy(reminderTime = time, reminderEnabled = true)
            }

        fun save() {
            val current = state.value
            if (current.saving) return
            if (!current.canSave) {
                state.update { it.copy(showErrors = true) }
                return
            }
            state.update { it.copy(saving = true) }
            viewModelScope.launch {
                val base = original ?: Habit(name = "", createdOn = LocalDate.now(clock))
                val habit =
                    base.copy(
                        name = current.name.trim(),
                        description = current.description.trim(),
                        icon = current.icon,
                        color = current.color,
                        dailyGoal = current.dailyGoal,
                        schedule = current.schedule,
                        reminder = current.reminderTime.takeIf { current.reminderEnabled },
                    )
                val id = habitRepository.saveHabit(habit)
                reminderScheduler.scheduleHabit(habit.copy(id = id))
                state.update { it.copy(saving = false, saved = true) }
            }
        }
    }
