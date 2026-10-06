package io.github.codingfulalt.cairn.screenshots

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnTheme
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import io.github.codingfulalt.cairn.feature.detail.HabitDetailContent
import io.github.codingfulalt.cairn.feature.editor.HabitEditorContent
import io.github.codingfulalt.cairn.feature.editor.HabitEditorViewModel
import io.github.codingfulalt.cairn.feature.habits.HabitsContent
import io.github.codingfulalt.cairn.feature.insights.InsightsContent
import io.github.codingfulalt.cairn.feature.onboarding.OnboardingContent
import io.github.codingfulalt.cairn.feature.onboarding.OnboardingUiState
import io.github.codingfulalt.cairn.feature.today.TodayActions
import io.github.codingfulalt.cairn.feature.today.TodayContent
import io.github.codingfulalt.cairn.navigation.TopLevelDestination
import io.github.codingfulalt.cairn.testing.FakeHabitRepository
import io.github.codingfulalt.cairn.testing.FakeReminderScheduler
import io.github.codingfulalt.cairn.ui.components.BOTTOM_BAR_CLEARANCE
import io.github.codingfulalt.cairn.ui.components.CairnBottomBar
import io.github.codingfulalt.cairn.ui.components.CairnNavRail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Clock
import java.time.LocalTime

private const val PHONE = "w393dp-h852dp-xhdpi"
private const val TABLET = "w1280dp-h800dp-land-hdpi"

/**
 * Renders the screenshots used in the README.
 * Re-record them with `./gradlew recordRoborazziDebug`.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = PHONE)
class ScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val todayActions = TodayActions({}, {}, {}, {}, {}, {}, {}, {})

    @Test
    fun todayLight() =
        capture("today-light") {
            PhoneShell(TopLevelDestination.Today) {
                TodayContent(SampleData.todayState, todayActions, it)
            }
        }

    @Test
    fun todayDark() =
        capture("today-dark", darkTheme = true) {
            PhoneShell(TopLevelDestination.Today) {
                TodayContent(SampleData.todayState, todayActions, it)
            }
        }

    @Test
    fun detail() =
        capture("detail") {
            HabitDetailContent(SampleData.detailState, {}, {}, {}, {}, {}, {})
        }

    @Test
    fun insights() =
        capture("insights", darkTheme = true) {
            PhoneShell(TopLevelDestination.Insights) {
                InsightsContent(SampleData.insightsState, {}, it)
            }
        }

    @Test
    fun habits() =
        capture("habits") {
            PhoneShell(TopLevelDestination.Habits) { HabitsContent(SampleData.habitsState, {}, it) }
        }

    @Test
    fun editor() {
        val viewModel =
            HabitEditorViewModel(
                SavedStateHandle(),
                FakeHabitRepository(),
                FakeReminderScheduler(),
                Clock.systemUTC(),
            )
        viewModel.onNameChange("Morning stretch")
        viewModel.onIconChange(HabitIcon.Workout)
        viewModel.onColorChange(HabitColor.Peach)
        viewModel.setSchedule(Habit.WEEKDAYS)
        viewModel.setReminderTime(LocalTime.of(7, 30))
        val state = viewModel.uiState.value
        capture("editor") { HabitEditorContent(state, viewModel, onClose = {}) }
    }

    @Test
    fun onboarding() =
        capture("onboarding") {
            OnboardingContent(OnboardingUiState(name = "Alex"), {}, {}, {}, Modifier.fillMaxSize())
        }

    @Test
    @Config(qualifiers = TABLET)
    fun tablet() =
        capture("tablet") {
            Row(Modifier.fillMaxSize()) {
                CairnNavRail(current = TopLevelDestination.Today, onSelect = {}, onAdd = {})
                Box(Modifier.weight(1f)) {
                    TodayContent(SampleData.todayState, todayActions, PaddingValues(bottom = 16.dp))
                }
            }
        }

    private fun capture(
        name: String,
        darkTheme: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            CairnTheme(darkTheme = darkTheme) {
                // stands in for the status bar, which Robolectric doesn't draw
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 24.dp),
                ) {
                    content()
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.onRoot().captureRoboImage("../docs/screenshots/$name.png")
    }
}

@Composable
private fun PhoneShell(
    current: TopLevelDestination,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        content(PaddingValues(bottom = BOTTOM_BAR_CLEARANCE))
        CairnBottomBar(
            current = current,
            onSelect = {},
            onAdd = {},
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
