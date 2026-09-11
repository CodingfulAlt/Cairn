package io.github.codingfulalt.cairn.core.domain

import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProgressCalculatorTest {
    private val today = LocalDate.of(2026, 9, 10)
    private val water =
        Habit(id = 1, name = "Water", dailyGoal = 2, createdOn = today.minusDays(10))
    private val read = Habit(id = 2, name = "Read", createdOn = today.minusDays(10))

    @Test
    fun halfDone_isNotPerfect() {
        val index = listOf(CheckIn(2, today, 1), CheckIn(1, today, 1)).toIndex()

        val progress = ProgressCalculator.dayProgress(listOf(water, read), index, today)

        assertEquals(2, progress.scheduled)
        assertEquals(1, progress.completed)
        assertEquals(0.5f, progress.ratio)
        assertFalse(progress.isPerfect)
    }

    @Test
    fun everythingDone_isPerfect() {
        val index = listOf(CheckIn(1, today, 2), CheckIn(2, today, 1)).toIndex()

        assertTrue(ProgressCalculator.dayProgress(listOf(water, read), index, today).isPerfect)
    }

    @Test
    fun habitsDoNotCountBeforeTheyExist() {
        val fresh = read.copy(createdOn = today)

        val progress = ProgressCalculator.dayProgress(listOf(fresh), emptyMap(), today.minusDays(1))

        assertEquals(0, progress.scheduled)
        assertFalse(progress.isPerfect)
    }

    @Test
    fun range_includesBothEnds() {
        val days = ProgressCalculator.range(listOf(read), emptyMap(), today.minusDays(6), today)

        assertEquals(7, days.size)
        assertEquals(today, days.last().date)
    }
}
