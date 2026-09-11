package io.github.codingfulalt.cairn.core.data.repository

import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>

    fun observeHabit(id: Long): Flow<Habit?>

    fun observeCheckIns(): Flow<List<CheckIn>>

    fun observeCheckIns(habitId: Long): Flow<List<CheckIn>>

    suspend fun getHabit(id: Long): Habit?

    suspend fun getActiveHabits(): List<Habit>

    suspend fun getCheckIns(date: LocalDate): List<CheckIn>

    /** Inserts when [Habit.id] is 0, updates otherwise. Returns the habit id. */
    suspend fun saveHabit(habit: Habit): Long

    suspend fun setArchived(
        id: Long,
        archived: Boolean,
    )

    suspend fun deleteHabit(id: Long)

    /** Adds [delta] to the day's count, kept between 0 and the habit's goal. Returns the new count. */
    suspend fun adjustCheckIn(
        habitId: Long,
        date: LocalDate,
        delta: Int,
    ): Int

    suspend fun deleteAll()
}
