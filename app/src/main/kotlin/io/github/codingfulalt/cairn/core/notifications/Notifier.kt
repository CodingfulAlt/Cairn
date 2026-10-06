package io.github.codingfulalt.cairn.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.model.Habit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Notifier
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private val manager = NotificationManagerCompat.from(context)

        fun createChannels() {
            val reminders =
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    context.getString(R.string.channel_reminders),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.channel_reminders_desc) }
            val summary =
                NotificationChannel(
                    CHANNEL_SUMMARY,
                    context.getString(R.string.channel_summary),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.channel_summary_desc) }
            context
                .getSystemService(
                    NotificationManager::class.java,
                ).createNotificationChannels(listOf(reminders, summary))
        }

        fun canPostNotifications(): Boolean =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                manager.areNotificationsEnabled()
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            }

        @SuppressLint("MissingPermission")
        fun showHabitReminder(
            habit: Habit,
            doneToday: Int,
        ) {
            if (!canPostNotifications()) return
            val markDone =
                PendingIntent.getBroadcast(
                    context,
                    habit.id.toInt(),
                    ReminderReceiver.markDoneIntent(context, habit.id),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            val actionLabel =
                if (habit.dailyGoal ==
                    1
                ) {
                    context.getString(R.string.notif_action_done)
                } else {
                    "+1"
                }
            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_REMINDERS)
                    .setSmallIcon(R.drawable.ic_stat_cairn)
                    .setContentTitle(habit.name)
                    .setContentText(
                        context.getString(R.string.notif_reminder_body, doneToday, habit.dailyGoal),
                    ).setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentIntent(openAppIntent())
                    .setAutoCancel(true)
                    .addAction(0, actionLabel, markDone)
                    .build()
            manager.notify(habitNotificationId(habit.id), notification)
        }

        @SuppressLint("MissingPermission")
        fun showDailySummary(remaining: Int) {
            if (!canPostNotifications()) return
            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_SUMMARY)
                    .setSmallIcon(R.drawable.ic_stat_cairn)
                    .setContentTitle(
                        context.resources.getQuantityString(
                            R.plurals.notif_summary_title,
                            remaining,
                            remaining,
                        ),
                    ).setContentText(context.getString(R.string.notif_summary_body))
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentIntent(openAppIntent())
                    .setAutoCancel(true)
                    .build()
            manager.notify(SUMMARY_NOTIFICATION_ID, notification)
        }

        fun cancelHabitReminder(habitId: Long) = manager.cancel(habitNotificationId(habitId))

        private fun openAppIntent(): PendingIntent {
            val launch =
                context.packageManager.getLaunchIntentForPackage(context.packageName)
                    ?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
            launch.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            return PendingIntent.getActivity(
                context,
                0,
                launch,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        private fun habitNotificationId(habitId: Long) = 1_000 + habitId.toInt()

        private companion object {
            const val CHANNEL_REMINDERS = "habit_reminders"
            const val CHANNEL_SUMMARY = "daily_summary"
            const val SUMMARY_NOTIFICATION_ID = 1
        }
    }
