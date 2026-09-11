package io.github.codingfulalt.cairn.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val icon: String,
    val color: String,
    @ColumnInfo(name = "daily_goal") val dailyGoal: Int,
    @ColumnInfo(name = "schedule_mask") val scheduleMask: Int,
    @ColumnInfo(name = "reminder_minute") val reminderMinute: Int?,
    @ColumnInfo(name = "created_on") val createdOn: Long,
    val archived: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

fun HabitEntity.asExternalModel() =
    Habit(
        id = id,
        name = name,
        description = description,
        icon = HabitIcon.fromKey(icon),
        color = HabitColor.fromKey(color),
        dailyGoal = dailyGoal,
        schedule = scheduleFromMask(scheduleMask),
        reminder = reminderMinute?.let { LocalTime.of(it / 60, it % 60) },
        createdOn = LocalDate.ofEpochDay(createdOn),
        archived = archived,
        sortOrder = sortOrder,
    )

fun Habit.asEntity() =
    HabitEntity(
        id = id,
        name = name.trim(),
        description = description.trim(),
        icon = icon.key,
        color = color.key,
        dailyGoal = dailyGoal,
        scheduleMask = schedule.toScheduleMask(),
        reminderMinute = reminder?.let { it.hour * 60 + it.minute },
        createdOn = createdOn.toEpochDay(),
        archived = archived,
        sortOrder = sortOrder,
    )

// bit 0 = Monday ... bit 6 = Sunday
internal fun Set<DayOfWeek>.toScheduleMask(): Int = fold(0) { mask, day -> mask or day.bit }

internal fun scheduleFromMask(mask: Int): Set<DayOfWeek> =
    DayOfWeek.entries
        .filter {
            (
                mask and
                    it.bit
            ) !=
                0
        }.toSet()

private val DayOfWeek.bit: Int get() = 1 shl (value - 1)
