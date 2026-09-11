package io.github.codingfulalt.cairn.feature.editor

import androidx.lifecycle.SavedStateHandle
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.testing.FakeHabitRepository
import io.github.codingfulalt.cairn.testing.FakeReminderScheduler
import io.github.codingfulalt.cairn.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class HabitEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 10)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val habits = FakeHabitRepository()
    private val scheduler = FakeReminderScheduler()

    private fun viewModel(habitId: Long = 0L) =
        HabitEditorViewModel(
            SavedStateHandle(mapOf("habitId" to habitId)),
            habits,
            scheduler,
            clock,
        )

    @Test
    fun blankName_isRejected() =
        runTest {
            val vm = viewModel()

            vm.save()

            assertTrue(vm.uiState.value.showErrors)
            assertFalse(vm.uiState.value.saved)
            assertTrue(habits.habits.value.isEmpty())
        }

    @Test
    fun newHabit_isSavedAndItsReminderScheduled() =
        runTest {
            val vm = viewModel()
            vm.onNameChange("  Stretch  ")
            vm.setReminderTime(LocalTime.of(7, 30))

            vm.save()

            val saved = habits.habits.value.single()
            assertEquals("Stretch", saved.name)
            assertEquals(today, saved.createdOn)
            assertEquals(LocalTime.of(7, 30), saved.reminder)
            assertEquals(saved.id, scheduler.scheduled.single().id)
            assertTrue(vm.uiState.value.saved)
        }

    @Test
    fun existingHabit_isLoadedIntoTheForm() =
        runTest {
            val id =
                habits.saveHabit(
                    Habit(name = "Walk", color = HabitColor.Sky, dailyGoal = 2, createdOn = today),
                )

            val state = viewModel(id).uiState.value

            assertFalse(state.isNew)
            assertEquals("Walk", state.name)
            assertEquals(HabitColor.Sky, state.color)
            assertEquals(2, state.dailyGoal)
        }

    @Test
    fun goal_staysInRange() {
        val vm = viewModel()

        vm.changeGoal(-5)
        assertEquals(1, vm.uiState.value.dailyGoal)

        repeat(50) { vm.changeGoal(1) }
        assertEquals(Habit.MAX_DAILY_GOAL, vm.uiState.value.dailyGoal)
    }
}
