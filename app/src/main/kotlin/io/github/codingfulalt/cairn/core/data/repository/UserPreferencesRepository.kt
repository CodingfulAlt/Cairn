package io.github.codingfulalt.cairn.core.data.repository

import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setUserName(name: String)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setDailySummary(
        enabled: Boolean,
        time: LocalTime,
    )
}
