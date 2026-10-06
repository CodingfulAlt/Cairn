package io.github.codingfulalt.cairn.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.codingfulalt.cairn.core.backup.BackupManager
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Provider

data class SettingsUiState(
    val loading: Boolean = true,
    val preferences: UserPreferences = UserPreferences(),
)

sealed interface BackupMessage {
    data class Exported(
        val habits: Int,
    ) : BackupMessage

    data class Restored(
        val habits: Int,
    ) : BackupMessage

    data object ExportFailed : BackupMessage

    data object ImportFailed : BackupMessage
}

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
        private val habitRepository: HabitRepository,
        private val reminderScheduler: ReminderScheduler,
        private val backupManager: BackupManager,
        private val clock: Provider<Clock>,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsUiState> =
            preferencesRepository.preferences
                .map { SettingsUiState(loading = false, preferences = it) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

        private val _backupMessage = MutableStateFlow<BackupMessage?>(null)
        val backupMessage: StateFlow<BackupMessage?> = _backupMessage.asStateFlow()

        fun backupFileName(): String = "cairn-backup-${LocalDate.now(clock.get())}.json"

        fun exportBackup(uri: Uri) {
            viewModelScope.launch {
                _backupMessage.value =
                    try {
                        BackupMessage.Exported(backupManager.export(uri))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        BackupMessage.ExportFailed
                    }
            }
        }

        fun importBackup(uri: Uri) {
            viewModelScope.launch {
                _backupMessage.value =
                    try {
                        BackupMessage.Restored(backupManager.import(uri))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        BackupMessage.ImportFailed
                    }
            }
        }

        fun backupMessageShown() {
            _backupMessage.value = null
        }

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
