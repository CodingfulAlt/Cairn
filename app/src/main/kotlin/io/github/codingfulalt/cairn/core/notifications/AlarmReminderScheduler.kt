package io.github.codingfulalt.cairn.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.domain.nextOccurrence
import io.github.codingfulalt.cairn.core.model.Habit
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

// Inexact alarms on purpose: a habit nudge a few minutes late is fine
// and we avoid asking for the exact alarm permission.
@Singleton
class AlarmReminderScheduler
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val habitRepository: HabitRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val clock: Provider<Clock>,
    ) : ReminderScheduler {
        private val alarmManager = context.getSystemService(AlarmManager::class.java)

        override fun scheduleHabit(habit: Habit) {
            val time = habit.reminder
            val next = time?.let { nextOccurrence(now(), it, habit.schedule) }
            if (habit.archived || next == null) {
                cancelHabit(habit.id)
                return
            }
            setAlarm(next, habitIntent(habit.id))
        }

        override fun cancelHabit(habitId: Long) {
            alarmManager.cancel(habitIntent(habitId))
        }

        override fun scheduleDailySummary(time: LocalTime) {
            val next = nextOccurrence(now(), time, Habit.EVERY_DAY) ?: return
            setAlarm(next, summaryIntent())
        }

        override fun cancelDailySummary() {
            alarmManager.cancel(summaryIntent())
        }

        override suspend fun rescheduleAll() {
            habitRepository.getActiveHabits().forEach(::scheduleHabit)
            val prefs = preferencesRepository.preferences.first()
            if (prefs.dailySummaryEnabled) {
                scheduleDailySummary(prefs.dailySummaryTime)
            } else {
                cancelDailySummary()
            }
        }

        private fun now() = ZonedDateTime.now(clock.get())

        private fun setAlarm(
            at: ZonedDateTime,
            operation: PendingIntent,
        ) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                at.toInstant().toEpochMilli(),
                operation,
            )
        }

        private fun habitIntent(habitId: Long): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                habitId.toInt(),
                ReminderReceiver.habitReminderIntent(context, habitId),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        private fun summaryIntent(): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                ReminderReceiver.summaryIntent(context),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
    }
