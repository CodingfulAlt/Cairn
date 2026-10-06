package io.github.codingfulalt.cairn.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.codingfulalt.cairn.core.data.repository.DataStoreUserPreferencesRepository
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.OfflineHabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindHabitRepository(impl: OfflineHabitRepository): HabitRepository

    @Binds
    abstract fun bindUserPreferencesRepository(
        impl: DataStoreUserPreferencesRepository,
    ): UserPreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile("user_preferences")
            }
    }
}
