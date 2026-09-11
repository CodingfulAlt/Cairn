package io.github.codingfulalt.cairn.core.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val icon: HabitIcon = HabitIcon.Sparkle,
    val color: HabitColor = HabitColor.Moss,
    val dailyGoal: Int = 1,
    val schedule: Set<DayOfWeek> = EVERY_DAY,
    val reminder: LocalTime? = null,
    val createdOn: LocalDate,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
) {
    fun isScheduledOn(date: LocalDate): Boolean = date.dayOfWeek in schedule

    // a habit counts for a day once it exists, or if it was checked in anyway
    fun isActiveOn(
        date: LocalDate,
        count: Int,
    ): Boolean = isScheduledOn(date) && (!date.isBefore(createdOn) || count > 0)

    companion object {
        const val MAX_DAILY_GOAL = 20
        const val MAX_NAME_LENGTH = 40
        const val MAX_NOTE_LENGTH = 120

        val EVERY_DAY: Set<DayOfWeek> = DayOfWeek.entries.toSet()
        val WEEKENDS: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        val WEEKDAYS: Set<DayOfWeek> = EVERY_DAY - WEEKENDS
    }
}

data class CheckIn(
    val habitId: Long,
    val date: LocalDate,
    val count: Int,
)
