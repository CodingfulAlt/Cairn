package io.github.codingfulalt.cairn.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.codingfulalt.cairn.core.common.di.ApplicationScope
import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.notifications.Notifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

// widgets and their actions are created by the system, so they pull dependencies from here
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun habitRepository(): HabitRepository

    fun notifier(): Notifier

    fun clock(): Clock
}

internal fun Context.widgetDependencies(): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)

/** The + button on a widget row. */
class CheckInAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val habitId = parameters[HabitId] ?: return
        val deps = context.widgetDependencies()
        val repository = deps.habitRepository()
        val habit = repository.getHabit(habitId) ?: return
        val count = repository.adjustCheckIn(habitId, LocalDate.now(deps.clock()), delta = 1)
        if (count >= habit.dailyGoal) deps.notifier().cancelHabitReminder(habitId)
        TodayWidget().update(context, glanceId)
    }

    companion object {
        val HabitId = ActionParameters.Key<Long>("habit_id")
    }
}

/** Redraws every widget whenever habits or check-ins change, from the app, a notification or a widget. */
@Singleton
class WidgetUpdater
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val habitRepository: HabitRepository,
        @param:ApplicationScope private val scope: CoroutineScope,
    ) {
        fun start() {
            scope.launch {
                combine(
                    habitRepository.observeHabits(),
                    habitRepository.observeCheckIns(),
                ) { _, _ -> }
                    .collectLatest {
                        // a short pause so a burst of taps turns into one redraw
                        delay(300)
                        try {
                            TodayWidget().updateAll(context)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            // a widget that fails to redraw should never take the app down
                        }
                    }
            }
        }
    }
