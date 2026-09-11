package io.github.codingfulalt.cairn.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject

class DataStoreUserPreferencesRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : UserPreferencesRepository {
        override val preferences: Flow<UserPreferences> =
            dataStore.data
                .catch { error ->
                    if (error is IOException) emit(emptyPreferences()) else throw error
                }.map { prefs ->
                    UserPreferences(
                        themeMode =
                            ThemeMode.entries.firstOrNull { it.name == prefs[THEME_MODE] }
                                ?: ThemeMode.System,
                        userName = prefs[USER_NAME].orEmpty(),
                        onboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: false,
                        dailySummaryEnabled = prefs[SUMMARY_ENABLED] ?: false,
                        dailySummaryTime =
                            prefs[SUMMARY_MINUTE]?.let { LocalTime.of(it / 60, it % 60) }
                                ?: UserPreferences.DEFAULT_SUMMARY_TIME,
                    )
                }

        override suspend fun setThemeMode(mode: ThemeMode) {
            dataStore.edit { it[THEME_MODE] = mode.name }
        }

        override suspend fun setUserName(name: String) {
            dataStore.edit { it[USER_NAME] = name.trim() }
        }

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
        }

        override suspend fun setDailySummary(
            enabled: Boolean,
            time: LocalTime,
        ) {
            dataStore.edit {
                it[SUMMARY_ENABLED] = enabled
                it[SUMMARY_MINUTE] = time.hour * 60 + time.minute
            }
        }

        private companion object {
            val THEME_MODE = stringPreferencesKey("theme_mode")
            val USER_NAME = stringPreferencesKey("user_name")
            val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
            val SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
            val SUMMARY_MINUTE = intPreferencesKey("daily_summary_minute")
        }
    }
