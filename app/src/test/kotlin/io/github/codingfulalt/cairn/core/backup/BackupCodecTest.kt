package io.github.codingfulalt.cairn.core.backup

import io.github.codingfulalt.cairn.core.model.CheckIn
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class BackupCodecTest {
    private val start = LocalDate.of(2026, 9, 1)

    private val water =
        Habit(
            id = 3,
            name = "Drink water",
            description = "Big glass",
            icon = HabitIcon.fromKey("water"),
            color = HabitColor.Sky,
            dailyGoal = 6,
            schedule = Habit.WEEKDAYS,
            reminder = LocalTime.of(9, 30),
            createdOn = start,
            sortOrder = 1,
        )
    private val read = Habit(id = 7, name = "Read 10 pages", createdOn = start, archived = true)

    private val prefs =
        UserPreferences(
            themeMode = ThemeMode.Dark,
            userName = "Alex",
            onboardingCompleted = true,
            dailySummaryEnabled = true,
            dailySummaryTime = LocalTime.of(21, 15),
        )

    @Test
    fun roundTripKeepsEverything() {
        val checkIns =
            listOf(
                CheckIn(3, start, 4),
                CheckIn(3, start.plusDays(1), 6),
                CheckIn(7, start, 1),
            )
        val text = BackupCodec.encode(listOf(water, read), checkIns, prefs, Instant.EPOCH)

        val content = BackupCodec.decode(text)

        assertEquals(listOf(water, read), content.habits)
        assertEquals(checkIns, content.checkIns)
        assertEquals(prefs, content.preferences)
    }

    @Test(expected = InvalidBackupException::class)
    fun rejectsRandomJson() {
        BackupCodec.decode("""{"hello": "world"}""")
    }

    @Test(expected = InvalidBackupException::class)
    fun rejectsSomethingThatIsNotJson() {
        BackupCodec.decode("not a backup at all")
    }

    @Test(expected = InvalidBackupException::class)
    fun rejectsBackupsFromANewerVersion() {
        BackupCodec.decode("""{"format": "cairn-backup", "version": 99}""")
    }

    @Test
    fun cleansUpBadEntries() {
        val text =
            """
            {
              "format": "cairn-backup",
              "version": 1,
              "habits": [
                {"id": 1, "name": "  Stretch  ", "dailyGoal": 500, "schedule": [], "createdOn": "2026-09-01"},
                {"id": 2, "name": "   ", "createdOn": "2026-09-01"},
                {"id": 0, "name": "No id", "createdOn": "2026-09-01"},
                {"id": 4, "name": "Bad date", "createdOn": "yesterday"}
              ],
              "checkIns": [
                {"habitId": 1, "date": "2026-09-02", "count": 900},
                {"habitId": 1, "date": "2026-09-03", "count": 0},
                {"habitId": 2, "date": "2026-09-02", "count": 1},
                {"habitId": 1, "date": "not a date", "count": 1}
              ]
            }
            """.trimIndent()

        val content = BackupCodec.decode(text)

        val habit = content.habits.single()
        assertEquals("Stretch", habit.name)
        assertEquals(Habit.MAX_DAILY_GOAL, habit.dailyGoal)
        assertEquals(Habit.EVERY_DAY, habit.schedule)
        assertEquals(
            listOf(CheckIn(1, LocalDate.of(2026, 9, 2), Habit.MAX_DAILY_GOAL)),
            content.checkIns,
        )
        assertNull(content.preferences)
    }

    @Test
    fun ignoresFieldsFromLaterVersions() {
        val text =
            """{"format": "cairn-backup", "version": 1, "somethingNew": true,
               "habits": [{"id": 5, "name": "Walk", "createdOn": "2026-09-01", "extra": 1}]}"""

        assertTrue(
            BackupCodec
                .decode(text)
                .habits
                .single()
                .name == "Walk",
        )
    }
}
