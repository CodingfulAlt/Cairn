package io.github.codingfulalt.cairn.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.codingfulalt.cairn.MainActivity
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.domain.DayProgress
import io.github.codingfulalt.cairn.core.domain.ProgressCalculator
import java.time.LocalDate

// below this height the widget only shows the count and the bar
private val LIST_MIN_HEIGHT = 150.dp

private val White = ColorProvider(Color.White)
private val SoftWhite = ColorProvider(Color.White.copy(alpha = 0.82f))
private val Track = ColorProvider(Color.White.copy(alpha = 0.3f))

internal data class WidgetHabit(
    val id: Long,
    val name: String,
    val color: Color,
    val count: Int,
    val goal: Int,
) {
    val done: Boolean get() = count >= goal
}

internal data class WidgetState(
    val progress: DayProgress,
    val habits: List<WidgetHabit>,
)

/** Today's progress on the home screen, with a + button to check in without opening the app. */
class TodayWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val state = loadState(context)
        provideContent { WidgetContent(state) }
    }

    private suspend fun loadState(context: Context): WidgetState {
        val deps = context.widgetDependencies()
        val repository = deps.habitRepository()
        val today = LocalDate.now(deps.clock())
        val habits = repository.getActiveHabits()
        val counts = repository.getCheckIns(today).associate { it.habitId to it.count }
        val todays =
            habits
                .filter { it.isActiveOn(today, counts[it.id] ?: 0) }
                .map {
                    WidgetHabit(
                        it.id,
                        it.name,
                        it.color.swatch,
                        counts[it.id] ?: 0,
                        it.dailyGoal,
                    )
                }.sortedBy { it.done }
        return WidgetState(
            progress = ProgressCalculator.dayProgress(habits, mapOf(today to counts), today),
            habits = todays,
        )
    }
}

@Composable
// scrollable = false draws the rows in a plain column, only the screenshot test needs that
internal fun WidgetContent(
    state: WidgetState,
    scrollable: Boolean = true,
) {
    val context = LocalContext.current
    val showList = LocalSize.current.height >= LIST_MIN_HEIGHT
    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ImageProvider(R.drawable.widget_background))
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.today_stack_label),
                    style =
                        TextStyle(
                            color = SoftWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    maxLines = 1,
                )
                Text(
                    text =
                        context.getString(
                            R.string.today_stack_count,
                            state.progress.completed,
                            state.progress.scheduled,
                        ),
                    style =
                        TextStyle(
                            color = White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    maxLines = 1,
                )
            }
            Image(
                provider = ImageProvider(R.drawable.widget_stones),
                contentDescription = null,
                modifier = GlanceModifier.size(width = 54.dp, height = 38.dp),
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        LinearProgressIndicator(
            progress = state.progress.ratio,
            modifier = GlanceModifier.fillMaxWidth().height(6.dp),
            color = White,
            backgroundColor = Track,
        )
        if (showList) {
            Spacer(GlanceModifier.height(10.dp))
            if (state.habits.isEmpty()) {
                Text(
                    text = context.getString(R.string.widget_empty),
                    style = TextStyle(color = SoftWhite, fontSize = 13.sp),
                )
            } else {
                if (scrollable) {
                    LazyColumn(GlanceModifier.fillMaxSize()) {
                        items(state.habits, itemId = { it.id }) { habit -> HabitRow(habit) }
                    }
                } else {
                    Column { state.habits.forEach { HabitRow(it) } }
                }
            }
        }
    }
}

@Composable
private fun HabitRow(habit: WidgetHabit) {
    val context = LocalContext.current
    Box(GlanceModifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(
            modifier =
                GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(R.drawable.widget_row_background))
                    .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.widget_dot),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ColorProvider(habit.color)),
                modifier = GlanceModifier.size(10.dp),
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = habit.name,
                style = TextStyle(color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
            )
            if (habit.goal > 1) {
                Text(
                    text = "${habit.count}/${habit.goal}",
                    style = TextStyle(color = SoftWhite, fontSize = 12.sp),
                    modifier = GlanceModifier.padding(horizontal = 6.dp),
                )
            }
            val buttonShape =
                if (habit.done) R.drawable.widget_button_done else R.drawable.widget_button
            val button = GlanceModifier.size(30.dp).background(ImageProvider(buttonShape))
            Box(
                modifier =
                    if (habit.done) {
                        button
                    } else {
                        button.clickable(
                            actionRunCallback<CheckInAction>(
                                actionParametersOf(
                                    CheckInAction.HabitId to habit.id,
                                ),
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider =
                        ImageProvider(
                            if (habit.done) R.drawable.widget_check else R.drawable.widget_plus,
                        ),
                    contentDescription =
                        context.getString(
                            if (habit.done) R.string.widget_done else R.string.widget_check_in,
                            habit.name,
                        ),
                    modifier = GlanceModifier.size(16.dp),
                )
            }
        }
    }
}
