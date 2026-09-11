package io.github.codingfulalt.cairn.feature.onboarding

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

enum class HabitSuggestion(
    @param:StringRes val title: Int,
    val icon: HabitIcon,
    val color: HabitColor,
    val dailyGoal: Int = 1,
) {
    Water(R.string.suggestion_water, HabitIcon.Water, HabitColor.Sky, dailyGoal = 6),
    Read(R.string.suggestion_read, HabitIcon.Book, HabitColor.Lavender),
    Move(R.string.suggestion_move, HabitIcon.Run, HabitColor.Moss),
    Meditate(R.string.suggestion_meditate, HabitIcon.Meditate, HabitColor.Mint),
    Sleep(R.string.suggestion_sleep, HabitIcon.Sleep, HabitColor.Peach),
    Journal(R.string.suggestion_journal, HabitIcon.Write, HabitColor.Sand),
}

data class OnboardingUiState(
    val name: String = "",
    val selected: Set<HabitSuggestion> = setOf(HabitSuggestion.Water, HabitSuggestion.Read),
    val saving: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val state = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = state.asStateFlow()

        fun onNameChange(name: String) {
            state.update { it.copy(name = name.take(Habit.MAX_NAME_LENGTH)) }
        }

        fun toggle(suggestion: HabitSuggestion) {
            state.update {
                val selected =
                    if (suggestion in
                        it.selected
                    ) {
                        it.selected - suggestion
                    } else {
                        it.selected + suggestion
                    }
                it.copy(selected = selected)
            }
        }

        fun finish(titleOf: (HabitSuggestion) -> String) {
            if (state.value.saving) return
            state.update { it.copy(saving = true) }
            val snapshot = state.value
            viewModelScope.launch {
                val today = LocalDate.now(clock)
                snapshot.selected.sortedBy { it.ordinal }.forEach { suggestion ->
                    habitRepository.saveHabit(
                        Habit(
                            name = titleOf(suggestion),
                            icon = suggestion.icon,
                            color = suggestion.color,
                            dailyGoal = suggestion.dailyGoal,
                            createdOn = today,
                        ),
                    )
                }
                preferencesRepository.setUserName(snapshot.name)
                preferencesRepository.setOnboardingCompleted(true)
            }
        }
    }
