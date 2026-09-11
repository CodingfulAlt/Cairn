package io.github.codingfulalt.cairn.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.component.EmptyState
import io.github.codingfulalt.cairn.core.designsystem.component.HabitBadge
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.ui.scheduleLabel
import kotlin.math.roundToInt

@Composable
fun HabitsScreen(
    onOpenHabit: (Long) -> Unit,
    contentPadding: PaddingValues,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HabitsContent(state, onOpenHabit, contentPadding)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitsContent(
    state: HabitsUiState,
    onOpenHabit: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    var showArchived by rememberSaveable { mutableStateOf(false) }
    val habits = if (showArchived) state.archived else state.active
    val (emptyTitle, emptyBody) =
        if (showArchived) {
            R.string.habits_archived_empty_title to R.string.habits_archived_empty_body
        } else {
            R.string.habits_empty_title to R.string.habits_empty_body
        }
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentPadding =
            PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = top,
                bottom = contentPadding.calculateBottomPadding() + 8.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    stringResource(R.string.habits_title),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(16.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !showArchived,
                        onClick = { showArchived = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) {
                        Text("${stringResource(R.string.habits_active)} · ${state.active.size}")
                    }
                    SegmentedButton(
                        selected = showArchived,
                        onClick = { showArchived = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) {
                        Text("${stringResource(R.string.habits_archived)} · ${state.archived.size}")
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        if (!state.loading && habits.isEmpty()) {
            item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    title = stringResource(emptyTitle),
                    body = stringResource(emptyBody),
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                )
            }
        }

        items(habits, key = { it.habit.id }) { summary ->
            HabitSummaryCard(summary, onClick = {
                onOpenHabit(summary.habit.id)
            }, modifier = Modifier.animateItem())
        }
    }
}

@Composable
private fun HabitSummaryCard(
    summary: HabitSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = summary.habit
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HabitBadge(habit.icon, habit.color, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text =
                            scheduleLabel(habit.schedule) + " · " +
                                pluralStringResource(
                                    R.plurals.goal_per_day,
                                    habit.dailyGoal,
                                    habit.dailyGoal,
                                ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                StreakPill(summary.stats.currentStreak)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                summary.lastSevenDays.forEach { mark ->
                    DayTick(mark, habit.color.swatch, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row {
                Text(
                    text = stringResource(R.string.habits_best, summary.stats.bestStreak),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${(summary.stats.completionRate * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StreakPill(streak: Int) {
    Row(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint =
                if (streak >
                    0
                ) {
                    Color(0xFFFF8A4C)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(streak.toString(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun DayTick(
    mark: DayMark,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val base = modifier.height(10.dp).clip(CircleShape)
    when (mark) {
        DayMark.Done -> Box(base.background(color))
        DayMark.Missed -> Box(base.background(colors.outlineVariant))
        DayMark.Pending -> Box(base.border(1.5.dp, colors.outline, CircleShape))
        DayMark.Off -> Box(base.background(colors.surfaceContainerHighest.copy(alpha = 0.5f)))
    }
}
