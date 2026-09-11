package io.github.codingfulalt.cairn.testing

import io.github.codingfulalt.cairn.core.data.repository.HabitRepository
import io.github.codingfulalt.cairn.core.data.repository.UserPreferencesRepository
import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import io.github.codingfulalt.cairn.core.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime

class FakeHabitRepository : HabitRepository {
    val habits = MutableStateFlow<List<Habit>>(emptyList())
    val checkIns = MutableStateFlow<List<CheckIn>>(emptyList())
    private var nextId = 1L

    override fun observeHabits(): Flow<List<Habit>> = habits

    override fun observeHabit(id: Long): Flow<Habit?> =
        habits.map { list -> list.firstOrNull { it.id == id } }

    override fun observeCheckIns(): Flow<List<CheckIn>> = checkIns

    override fun observeCheckIns(habitId: Long): Flow<List<CheckIn>> =
        checkIns.map { list -> list.filter { it.habitId == habitId } }

    override suspend fun getHabit(id: Long): Habit? = habits.value.firstOrNull { it.id == id }

    override suspend fun getActiveHabits(): List<Habit> = habits.value.filterNot { it.archived }

    override suspend fun getCheckIns(date: LocalDate): List<CheckIn> =
        checkIns.value.filter { it.date == date }

    override suspend fun saveHabit(habit: Habit): Long {
        if (habit.id != 0L) {
            habits.update { list -> list.map { if (it.id == habit.id) habit else it } }
            return habit.id
        }
        val id = nextId++
        habits.update { it + habit.copy(id = id) }
        return id
    }

    override suspend fun setArchived(
        id: Long,
        archived: Boolean,
    ) = habits.update { list -> list.map { if (it.id == id) it.copy(archived = archived) else it } }

    override suspend fun deleteHabit(id: Long) {
        habits.update { list -> list.filterNot { it.id == id } }
        checkIns.update { list -> list.filterNot { it.habitId == id } }
    }

    override suspend fun adjustCheckIn(
        habitId: Long,
        date: LocalDate,
        delta: Int,
    ): Int {
        val goal = getHabit(habitId)?.dailyGoal ?: return 0
        val current =
            checkIns.value.firstOrNull { it.habitId == habitId && it.date == date }?.count ?: 0
        val updated = (current + delta).coerceIn(0, goal)
        checkIns.update { list ->
            list.filterNot { it.habitId == habitId && it.date == date } +
                listOfNotNull(CheckIn(habitId, date, updated).takeIf { updated > 0 })
        }
        return updated
    }

    override suspend fun deleteAll() {
        habits.value = emptyList()
        checkIns.value = emptyList()
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val preferences = MutableStateFlow(UserPreferences())

    override suspend fun setThemeMode(mode: ThemeMode) =
        preferences.update { it.copy(themeMode = mode) }

    override suspend fun setUserName(name: String) =
        preferences.update { it.copy(userName = name.trim()) }

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        preferences.update { it.copy(onboardingCompleted = completed) }

    override suspend fun setDailySummary(
        enabled: Boolean,
        time: LocalTime,
    ) = preferences.update { it.copy(dailySummaryEnabled = enabled, dailySummaryTime = time) }
}

class FakeReminderScheduler : ReminderScheduler {
    val scheduled = mutableListOf<Habit>()
    val cancelled = mutableListOf<Long>()

    override fun scheduleHabit(habit: Habit) {
        scheduled += habit
    }

    override fun cancelHabit(habitId: Long) {
        cancelled += habitId
    }

    override fun scheduleDailySummary(time: LocalTime) = Unit

    override fun cancelDailySummary() = Unit

    override suspend fun rescheduleAll() = Unit
}
