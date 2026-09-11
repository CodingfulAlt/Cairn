package io.github.codingfulalt.cairn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        preferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        // Eagerly so the splash screen can read it before anything collects
        val uiState: StateFlow<MainUiState> =
            preferencesRepository.preferences
                .map { MainUiState.Ready(it.themeMode, it.onboardingCompleted) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState.Loading)
    }

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Ready(
        val themeMode: ThemeMode,
        val onboardingCompleted: Boolean,
    ) : MainUiState
}
