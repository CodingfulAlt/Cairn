package io.github.codingfulalt.cairn.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val loading: Boolean = true,
    val preferences: UserPreferences = UserPreferences(),
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
        private val habitRepository: HabitRepository,
        private val reminderScheduler: ReminderScheduler,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsUiState> =
            preferencesRepository.preferences
                .map { SettingsUiState(loading = false, preferences = it) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

        fun setThemeMode(mode: ThemeMode) {
            viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
        }

        fun setUserName(name: String) {
            viewModelScope.launch { preferencesRepository.setUserName(name) }
        }

        fun setDailySummary(
            enabled: Boolean,
            time: LocalTime,
        ) {
            viewModelScope.launch {
                preferencesRepository.setDailySummary(enabled, time)
                if (enabled) {
                    reminderScheduler.scheduleDailySummary(
                        time,
                    )
                } else {
                    reminderScheduler.cancelDailySummary()
                }
            }
        }

        fun eraseAllData() {
            viewModelScope.launch {
                habitRepository.observeHabits().first().forEach {
                    reminderScheduler.cancelHabit(
                        it.id,
                    )
                }
                habitRepository.deleteAll()
            }
        }
    }
