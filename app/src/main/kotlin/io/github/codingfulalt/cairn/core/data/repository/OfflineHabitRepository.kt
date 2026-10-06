package io.github.codingfulalt.cairn.core.data.repository

import io.github.codingfulalt.cairn.core.database.dao.CheckInDao
import io.github.codingfulalt.cairn.core.database.dao.HabitDao
import io.github.codingfulalt.cairn.core.database.entity.CheckInEntity
import io.github.codingfulalt.cairn.core.database.entity.HabitEntity
import io.github.codingfulalt.cairn.core.database.entity.asEntity
import io.github.codingfulalt.cairn.core.database.entity.asExternalModel
import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class OfflineHabitRepository
    @Inject
    constructor(
        private val habitDao: HabitDao,
        private val checkInDao: CheckInDao,
    ) : HabitRepository {
        override fun observeHabits(): Flow<List<Habit>> =
            habitDao.observeAll().map {
                it.map(HabitEntity::asExternalModel)
            }

        override fun observeHabit(id: Long): Flow<Habit?> =
            habitDao.observe(id).map {
                it?.asExternalModel()
            }

        override fun observeCheckIns(): Flow<List<CheckIn>> =
            checkInDao.observeAll().map {
                it.map(CheckInEntity::asExternalModel)
            }

        override fun observeCheckIns(habitId: Long): Flow<List<CheckIn>> =
            checkInDao.observeForHabit(habitId).map { it.map(CheckInEntity::asExternalModel) }

        override suspend fun getHabit(id: Long): Habit? = habitDao.get(id)?.asExternalModel()

        override suspend fun getActiveHabits(): List<Habit> =
            habitDao.getActive().map(HabitEntity::asExternalModel)

        override suspend fun getCheckIns(date: LocalDate): List<CheckIn> =
            checkInDao.getForDay(date.toEpochDay()).map(CheckInEntity::asExternalModel)

        override suspend fun saveHabit(habit: Habit): Long =
            if (habit.id == 0L) {
                habitDao.insert(habit.copy(sortOrder = habitDao.nextSortOrder()).asEntity())
            } else {
                habitDao.update(habit.asEntity())
                habit.id
            }

        override suspend fun setArchived(
            id: Long,
            archived: Boolean,
        ) = habitDao.setArchived(id, archived)

        override suspend fun deleteHabit(id: Long) = habitDao.delete(id)

        override suspend fun adjustCheckIn(
            habitId: Long,
            date: LocalDate,
            delta: Int,
        ): Int {
            val goal = habitDao.get(habitId)?.dailyGoal ?: return 0
            return checkInDao.adjust(habitId, date.toEpochDay(), delta, goal)
        }

        override suspend fun deleteAll() = habitDao.deleteAll()
    }
