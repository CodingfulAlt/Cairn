package io.github.codingfulalt.cairn.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.github.codingfulalt.cairn.core.model.CheckIn
import java.time.LocalDate

@Entity(
    tableName = "check_ins",
    primaryKeys = ["habit_id", "epoch_day"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("epoch_day")],
)
data class CheckInEntity(
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val count: Int,
)

fun CheckInEntity.asExternalModel() = CheckIn(habitId, LocalDate.ofEpochDay(epochDay), count)
