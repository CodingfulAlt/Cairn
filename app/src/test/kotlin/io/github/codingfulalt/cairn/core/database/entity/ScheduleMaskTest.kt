package io.github.codingfulalt.cairn.core.database.entity

import io.github.codingfulalt.cairn.core.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class ScheduleMaskTest {
    @Test
    fun everyDay_usesAllSevenBits() {
        assertEquals(0b1111111, Habit.EVERY_DAY.toScheduleMask())
    }

    @Test
    fun mondayIsTheLowestBit_sundayTheHighest() {
        assertEquals(1, setOf(DayOfWeek.MONDAY).toScheduleMask())
        assertEquals(64, setOf(DayOfWeek.SUNDAY).toScheduleMask())
    }

    @Test
    fun roundTrip_keepsTheSameDays() {
        listOf(
            Habit.WEEKDAYS,
            Habit.WEEKENDS,
            setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY),
            emptySet(),
        ).forEach { days ->
            assertEquals(days, scheduleFromMask(days.toScheduleMask()))
        }
    }
}
