package io.github.codingfulalt.cairn.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val deps =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                NotificationsEntryPoint::class.java,
            )
        val pendingResult = goAsync()
        deps.applicationScope().launch {
            try {
                handle(intent, deps)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(
        intent: Intent,
        deps: NotificationsEntryPoint,
    ) {
        val today = LocalDate.now(deps.clock())
        val habits = deps.habitRepository()
        when (intent.action) {
            ACTION_HABIT_REMINDER -> {
                val habit = habits.getHabit(intent.getLongExtra(EXTRA_HABIT_ID, 0)) ?: return
                if (habit.archived) return
                deps.reminderScheduler().scheduleHabit(habit)
                if (!habit.isScheduledOn(today)) return
                val count =
                    habits.getCheckIns(today).firstOrNull { it.habitId == habit.id }?.count ?: 0
                if (count < habit.dailyGoal) deps.notifier().showHabitReminder(habit, count)
            }

            ACTION_DAILY_SUMMARY -> {
                val prefs = deps.preferencesRepository().preferences.first()
                if (!prefs.dailySummaryEnabled) return
                deps.reminderScheduler().scheduleDailySummary(prefs.dailySummaryTime)
                val counts = habits.getCheckIns(today).associate { it.habitId to it.count }
                val remaining =
                    habits.getActiveHabits().count { habit ->
                        val count = counts[habit.id] ?: 0
                        habit.isActiveOn(today, count) && count < habit.dailyGoal
                    }
                if (remaining > 0) deps.notifier().showDailySummary(remaining)
            }

            ACTION_MARK_DONE -> {
                val habitId = intent.getLongExtra(EXTRA_HABIT_ID, 0)
                habits.adjustCheckIn(habitId, today, delta = 1)
                deps.notifier().cancelHabitReminder(habitId)
            }
        }
    }

    companion object {
        private const val ACTION_PREFIX = "io.github.codingfulalt.cairn.action"
        private const val ACTION_HABIT_REMINDER = "$ACTION_PREFIX.HABIT_REMINDER"
        private const val ACTION_DAILY_SUMMARY = "$ACTION_PREFIX.DAILY_SUMMARY"
        private const val ACTION_MARK_DONE = "$ACTION_PREFIX.MARK_DONE"
        private const val EXTRA_HABIT_ID = "habit_id"

        fun habitReminderIntent(
            context: Context,
            habitId: Long,
        ): Intent =
            Intent(context, ReminderReceiver::class.java)
                .setAction(ACTION_HABIT_REMINDER)
                .putExtra(EXTRA_HABIT_ID, habitId)

        fun summaryIntent(context: Context): Intent =
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_DAILY_SUMMARY)

        fun markDoneIntent(
            context: Context,
            habitId: Long,
        ): Intent =
            Intent(context, ReminderReceiver::class.java)
                .setAction(ACTION_MARK_DONE)
                .putExtra(EXTRA_HABIT_ID, habitId)
    }
}
