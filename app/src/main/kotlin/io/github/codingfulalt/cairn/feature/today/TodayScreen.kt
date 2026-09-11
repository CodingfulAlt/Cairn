package io.github.codingfulalt.cairn.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.component.CairnStack
import io.github.codingfulalt.cairn.core.designsystem.component.ConfettiBurst
import io.github.codingfulalt.cairn.core.designsystem.component.EmptyState
import io.github.codingfulalt.cairn.core.designsystem.component.SectionHeader
import io.github.codingfulalt.cairn.core.designsystem.component.Stone
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.ui.HabitCard
import io.github.codingfulalt.cairn.core.ui.currentLocale
import io.github.codingfulalt.cairn.core.ui.fullDate
import io.github.codingfulalt.cairn.core.ui.greeting
import io.github.codingfulalt.cairn.feature.today.components.TodayHero
import io.github.codingfulalt.cairn.feature.today.components.WeekStrip
import java.time.LocalDate

private val WIDE_LAYOUT = 840.dp
private val CONFETTI_COLORS = HabitColor.entries.map { it.swatch } + CairnPalette.Ink

@Composable
fun TodayScreen(
    onOpenHabit: (Long) -> Unit,
    onCreateHabit: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayContent(
        state = state,
        actions =
            TodayActions(
                openHabit = onOpenHabit,
                createHabit = onCreateHabit,
                selectDate = viewModel::selectDate,
                previousWeek = viewModel::previousWeek,
                nextWeek = viewModel::nextWeek,
                jumpToToday = viewModel::jumpToToday,
                checkIn = viewModel::checkIn,
                undo = viewModel::undoCheckIn,
            ),
        contentPadding = contentPadding,
    )
}

internal class TodayActions(
    val openHabit: (Long) -> Unit,
    val createHabit: () -> Unit,
    val selectDate: (LocalDate) -> Unit,
    val previousWeek: () -> Unit,
    val nextWeek: () -> Unit,
    val jumpToToday: () -> Unit,
    val checkIn: (Long) -> Unit,
    val undo: (Long) -> Unit,
)

@Composable
internal fun TodayContent(
    state: TodayUiState,
    actions: TodayActions,
    contentPadding: PaddingValues,
) {
    var confetti by remember { mutableIntStateOf(0) }
    var wasPerfectToday by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(state.progress, state.isViewingToday, state.loading) {
        if (state.loading || !state.isViewingToday) return@LaunchedEffect
        val perfect = state.progress.isPerfect
        if (wasPerfectToday == false && perfect) confetti++
        wasPerfectToday = perfect
    }

    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp
    val bottom = contentPadding.calculateBottomPadding() + 8.dp

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= WIDE_LAYOUT) {
                WideToday(state, actions, top, bottom)
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopCenter,
                            ).widthIn(max = 680.dp)
                            .fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = top,
                            bottom = bottom,
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "overview") { Overview(state, actions) }
                    habitSections(state, actions)
                }
            }
        }
        ConfettiBurst(
            trigger = confetti,
            colors = CONFETTI_COLORS,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun WideToday(
    state: TodayUiState,
    actions: TodayActions,
    top: Dp,
    bottom: Dp,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Column(
            Modifier
                .width(400.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = top, bottom = bottom),
        ) {
            Overview(state, actions)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(top = top + 8.dp, bottom = bottom),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            habitSections(state, actions)
        }
    }
}

@Composable
private fun Overview(
    state: TodayUiState,
    actions: TodayActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Header(userName = state.userName, hour = state.hour, today = state.today)
        WeekStrip(
            days = state.week,
            selected = state.selectedDate,
            canGoNext = state.canGoToNextWeek,
            onSelect = actions.selectDate,
            onPrevious = actions.previousWeek,
            onNext = actions.nextWeek,
        )
        if (state.hasHabits) {
            val stones =
                (state.done + state.pending).map {
                    Stone(
                        color = CairnPalette.Ink,
                        placed = it.isDone,
                    )
                }
            TodayHero(
                progress = state.progress,
                stones = stones,
                isViewingToday = state.isViewingToday,
                onJumpToToday = actions.jumpToToday,
            )
        }
    }
}

private fun LazyListScope.habitSections(
    state: TodayUiState,
    actions: TodayActions,
) {
    when {
        state.loading -> Unit

        !state.hasHabits ->
            item(key = "empty") {
                EmptyState(
                    title = stringResource(R.string.today_empty_title),
                    body = stringResource(R.string.today_empty_body),
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        Button(onClick = actions.createHabit, shape = CircleShape) {
                            Text(stringResource(R.string.today_empty_action))
                        }
                    },
                )
            }

        state.pending.isEmpty() && state.done.isEmpty() ->
            item(key = "rest") {
                EmptyState(
                    title = stringResource(R.string.today_rest_title),
                    body = stringResource(R.string.today_rest_body),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

        else -> {
            if (state.pending.isNotEmpty()) {
                item(key = "todo") {
                    SectionHeader(
                        title = stringResource(R.string.today_section_todo),
                        count = state.pending.size,
                        modifier = Modifier.animateItem().padding(top = 8.dp),
                    )
                }
                items(state.pending, key = { it.habit.id }) { item ->
                    TodayHabitCard(item, actions, Modifier.animateItem())
                }
            }
            if (state.done.isNotEmpty()) {
                item(key = "done") {
                    SectionHeader(
                        title = stringResource(R.string.today_section_done),
                        count = state.done.size,
                        modifier = Modifier.animateItem().padding(top = 8.dp),
                    )
                }
                items(state.done, key = { it.habit.id }) { item ->
                    TodayHabitCard(item, actions, Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun TodayHabitCard(
    item: TodayHabit,
    actions: TodayActions,
    modifier: Modifier = Modifier,
) {
    HabitCard(
        habit = item.habit,
        count = item.count,
        streak = item.streak,
        onClick = { actions.openHabit(item.habit.id) },
        onCheckIn = { actions.checkIn(item.habit.id) },
        onUndo = { actions.undo(item.habit.id) },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun Header(
    userName: String,
    hour: Int,
    today: LocalDate,
) {
    val hello = greeting(hour)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = today.fullDate(currentLocale()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text =
                    if (userName.isBlank()) {
                        hello
                    } else {
                        stringResource(
                            R.string.greeting_named,
                            hello,
                            userName,
                        )
                    },
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
            )
        }
        Spacer(Modifier.width(12.dp))
        Avatar(userName)
    }
}

@Composable
private fun Avatar(userName: String) {
    Box(
        modifier =
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        val initial = userName.trim().firstOrNull()
        if (initial != null) {
            Text(initial.uppercase(), style = MaterialTheme.typography.titleMedium)
        } else {
            CairnStack(
                stones = List(3) { Stone(MaterialTheme.colorScheme.onSurface, placed = true) },
                modifier = Modifier.size(width = 24.dp, height = 22.dp),
                outlineColor = Color.Transparent,
            )
        }
    }
}
