package io.github.codingfulalt.cairn.core.notifications.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.codingfulalt.cairn.core.notifications.AlarmReminderScheduler
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds
    abstract fun bindReminderScheduler(impl: AlarmReminderScheduler): ReminderScheduler
}
