package io.github.codingfulalt.cairn.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.codingfulalt.cairn.core.database.entity.HabitEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckInDaoTest {
    private lateinit var database: CairnDatabase

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CairnDatabase::class.java,
                ).build()
    }

    @After
    fun tearDown() = database.close()

    private suspend fun insertHabit(goal: Int) =
        database.habitDao().insert(
            HabitEntity(
                name = "Water",
                description = "",
                icon = "water",
                color = "sky",
                dailyGoal = goal,
                scheduleMask = 127,
                reminderMinute = null,
                createdOn = 0,
                archived = false,
                sortOrder = 0,
            ),
        )

    @Test
    fun adjust_neverGoesPastTheGoalOrBelowZero() =
        runBlocking {
            val id = insertHabit(goal = 2)
            val dao = database.checkInDao()

            repeat(3) { dao.adjust(id, epochDay = 100, delta = 1, max = 2) }
            assertEquals(2, dao.getCount(id, 100))

            dao.adjust(id, epochDay = 100, delta = -5, max = 2)
            assertNull(dao.getCount(id, 100))
        }

    @Test
    fun deletingAHabit_removesItsCheckIns() =
        runBlocking {
            val id = insertHabit(goal = 1)
            database.checkInDao().adjust(id, epochDay = 100, delta = 1, max = 1)

            database.habitDao().delete(id)

            assertTrue(database.checkInDao().getForDay(100).isEmpty())
        }
}
