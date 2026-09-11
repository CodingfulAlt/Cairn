package io.github.codingfulalt.cairn.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.codingfulalt.cairn.core.database.dao.CheckInDao
import io.github.codingfulalt.cairn.core.database.dao.HabitDao
import io.github.codingfulalt.cairn.core.database.entity.CheckInEntity
import io.github.codingfulalt.cairn.core.database.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CheckInEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CairnDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    abstract fun checkInDao(): CheckInDao
}
