package io.github.codingfulalt.cairn.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.codingfulalt.cairn.core.database.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CheckInDao {
    @Query("SELECT * FROM check_ins")
    abstract fun observeAll(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE habit_id = :habitId")
    abstract fun observeForHabit(habitId: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE epoch_day = :epochDay")
    abstract suspend fun getForDay(epochDay: Long): List<CheckInEntity>

    @Query("SELECT count FROM check_ins WHERE habit_id = :habitId AND epoch_day = :epochDay")
    abstract suspend fun getCount(
        habitId: Long,
        epochDay: Long,
    ): Int?

    @Upsert
    abstract suspend fun upsert(checkIn: CheckInEntity)

    @Query("DELETE FROM check_ins WHERE habit_id = :habitId AND epoch_day = :epochDay")
    abstract suspend fun delete(
        habitId: Long,
        epochDay: Long,
    )

    @Transaction
    open suspend fun adjust(
        habitId: Long,
        epochDay: Long,
        delta: Int,
        max: Int,
    ): Int {
        val updated = ((getCount(habitId, epochDay) ?: 0) + delta).coerceIn(0, max)
        if (updated == 0) {
            delete(habitId, epochDay)
        } else {
            upsert(CheckInEntity(habitId, epochDay, updated))
        }
        return updated
    }
}
