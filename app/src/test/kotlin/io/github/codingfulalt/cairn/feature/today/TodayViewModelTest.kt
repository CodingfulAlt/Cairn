package io.github.codingfulalt.cairn.feature.today

import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.testing.FakeHabitRepository
import io.github.codingfulalt.cairn.testing.FakeUserPreferencesRepository
import io.github.codingfulalt.cairn.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 10)
    private val clock = Clock.fixed(today.atTime(9, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val habits = FakeHabitRepository()

    // lazy so it's created after the rule swaps in the test Main dispatcher
    private val viewModel by lazy { TodayViewModel(habits, FakeUserPreferencesRepository(), clock) }

    private fun TestScope.collectState() {
        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler),
        ) { viewModel.uiState.collect {} }
    }

    @Test
    fun checkIn_movesHabitToDone() =
        runTest {
            collectState()
            val id = habits.saveHabit(Habit(name = "Read", createdOn = today.minusDays(3)))

            viewModel.checkIn(id)

            val state = viewModel.uiState.value
            val done = state.done.single()
            assertTrue(state.pending.isEmpty())
            assertEquals(id, done.habit.id)
            assertTrue(state.progress.isPerfect)
        }

    @Test
    fun undo_putsHabitBack() =
        runTest {
            collectState()
            val id = habits.saveHabit(Habit(name = "Read", createdOn = today))

            viewModel.checkIn(id)
            viewModel.undoCheckIn(id)

            val pending = viewModel.uiState.value.pending
            assertEquals(id, pending.single().habit.id)
        }

    @Test
    fun newHabit_isHiddenOnEarlierDays() =
        runTest {
            collectState()
            habits.saveHabit(Habit(name = "Read", createdOn = today))

            viewModel.selectDate(today.minusDays(1))

            val state = viewModel.uiState.value
            assertTrue(state.hasHabits)
            assertTrue(state.pending.isEmpty() && state.done.isEmpty())
        }

    @Test
    fun futureDates_cannotBeSelected() =
        runTest {
            collectState()

            viewModel.selectDate(today.plusDays(2))

            assertEquals(today, viewModel.uiState.value.selectedDate)
        }
}
