package io.github.codingfulalt.cairn.core.notifications

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.codingfulalt.cairn.core.common.di.ApplicationScope
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import java.time.Clock

// receivers are created by the system, so they pull dependencies from here
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationsEntryPoint {
    fun habitRepository(): HabitRepository

    fun preferencesRepository(): UserPreferencesRepository

    fun reminderScheduler(): ReminderScheduler

    fun notifier(): Notifier

    fun clock(): Clock

    @ApplicationScope
    fun applicationScope(): CoroutineScope
}
