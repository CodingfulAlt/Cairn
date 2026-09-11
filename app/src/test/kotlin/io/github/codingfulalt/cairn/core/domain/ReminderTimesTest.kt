package io.github.codingfulalt.cairn.core.domain

import io.github.codingfulalt.cairn.core.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderTimesTest {
    // Thursday 10:00
    private val now = ZonedDateTime.of(2026, 9, 10, 10, 0, 0, 0, ZoneId.of("Europe/Sofia"))

    @Test
    fun laterToday_firesToday() {
        val next = nextOccurrence(now, LocalTime.of(18, 30), Habit.EVERY_DAY)

        assertEquals(now.withHour(18).withMinute(30), next)
    }

    @Test
    fun earlierToday_firesTomorrow() {
        val next = nextOccurrence(now, LocalTime.of(8, 0), Habit.EVERY_DAY)

        assertEquals(now.plusDays(1).withHour(8), next)
    }

    @Test
    fun skipsDaysOffTheSchedule() {
        val next = nextOccurrence(now, LocalTime.of(9, 0), setOf(DayOfWeek.MONDAY))

        assertEquals(DayOfWeek.MONDAY, next?.dayOfWeek)
        assertEquals(14, next?.dayOfMonth)
    }

    @Test
    fun sameWeekdayAlreadyPassed_goesToNextWeek() {
        val next = nextOccurrence(now, LocalTime.of(7, 0), setOf(DayOfWeek.THURSDAY))

        assertEquals(17, next?.dayOfMonth)
    }

    @Test
    fun emptySchedule_neverFires() {
        assertNull(nextOccurrence(now, LocalTime.NOON, emptySet()))
    }
}
