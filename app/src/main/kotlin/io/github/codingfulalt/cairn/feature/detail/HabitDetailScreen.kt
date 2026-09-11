package io.github.codingfulalt.cairn.feature.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.component.EmptyState
import io.github.codingfulalt.cairn.core.designsystem.component.HabitBadge
import io.github.codingfulalt.cairn.core.designsystem.component.Heatmap
import io.github.codingfulalt.cairn.core.designsystem.component.HeatmapLegend
import io.github.codingfulalt.cairn.core.designsystem.component.ProgressRing
import io.github.codingfulalt.cairn.core.designsystem.component.StatTile
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.ui.formatTime
import io.github.codingfulalt.cairn.core.ui.scheduleLabel
import kotlin.math.roundToInt

private val WIDE_LAYOUT = 840.dp

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }
    HabitDetailContent(
        state = state,
        onBack = onBack,
        onEdit = onEdit,
        onCheckIn = viewModel::checkIn,
        onUndo = viewModel::undoCheckIn,
        onToggleArchived = viewModel::toggleArchived,
        onDelete = viewModel::delete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitDetailContent(
    state: HabitDetailUiState,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onCheckIn: () -> Unit,
    onUndo: () -> Unit,
    onToggleArchived: () -> Unit,
    onDelete: () -> Unit,
) {
    val habit = state.habit
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (habit != null) {
                        IconButton(onClick = { onEdit(habit.id) }) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.action_edit),
                            )
                        }
                        OverflowMenu(
                            habit = habit,
                            streak = state.stats.currentStreak,
                            onToggleArchived = onToggleArchived,
                            onDelete = { confirmDelete = true },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        when {
            state.loading || state.deleted -> Unit
            habit == null ->
                EmptyState(
                    title = stringResource(R.string.detail_missing),
                    body = "",
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            else ->
                BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
                    if (maxWidth >= WIDE_LAYOUT) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                        ) {
                            Column(
                                Modifier
                                    .weight(
                                        1f,
                                    ).verticalScroll(rememberScrollState())
                                    .navigationBarsPadding(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Header(habit)
                                TodayCard(habit, state.todayCount, onCheckIn, onUndo)
                                Stats(state, habit)
                            }
                            Column(
                                Modifier
                                    .weight(
                                        1f,
                                    ).verticalScroll(rememberScrollState())
                                    .navigationBarsPadding(),
                            ) {
                                History(state.heatmap, habit)
                            }
                        }
                    } else {
                        Column(
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .widthIn(max = 680.dp)
                                    .verticalScroll(rememberScrollState())
                                    .navigationBarsPadding()
                                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Header(habit)
                            TodayCard(habit, state.todayCount, onCheckIn, onUndo)
                            Stats(state, habit)
                            History(state.heatmap, habit)
                        }
                    }
                }
        }
    }

    if (confirmDelete && habit != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.detail_delete_title, habit.name)) },
            text = { Text(stringResource(R.string.detail_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                ) {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDelete = false },
                ) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun OverflowMenu(
    habit: Habit,
    streak: Int,
    onToggleArchived: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val shareText =
        stringResource(
            R.string.share_habit,
            habit.name,
            pluralStringResource(R.plurals.days_count, streak, streak),
        )
    val chooserTitle = stringResource(R.string.share_habit_chooser)
    val archiveLabel = if (habit.archived) R.string.action_restore else R.string.action_archive

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.action_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_share)) },
                leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                onClick = {
                    expanded = false
                    val send =
                        Intent(
                            Intent.ACTION_SEND,
                        ).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
                    context.startActivity(Intent.createChooser(send, chooserTitle))
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(archiveLabel)) },
                leadingIcon = {
                    Icon(
                        if (habit.archived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onToggleArchived()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(habit: Habit) {
    Column {
        HabitBadge(habit.icon, habit.color, size = 64.dp)
        Spacer(Modifier.height(16.dp))
        Text(habit.name, style = MaterialTheme.typography.headlineMedium)
        if (habit.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = habit.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(14.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoPill(Icons.Rounded.EventRepeat, scheduleLabel(habit.schedule))
            InfoPill(
                Icons.Rounded.Flag,
                pluralStringResource(R.plurals.goal_per_day, habit.dailyGoal, habit.dailyGoal),
            )
            habit.reminder?.let { InfoPill(Icons.Rounded.NotificationsActive, formatTime(it)) }
            if (habit.archived) {
                InfoPill(
                    Icons.Rounded.Archive,
                    stringResource(R.string.detail_archived),
                )
            }
        }
    }
}

@Composable
private fun InfoPill(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TodayCard(
    habit: Habit,
    count: Int,
    onCheckIn: () -> Unit,
    onUndo: () -> Unit,
) {
    val ink = CairnPalette.Ink
    val swatch = habit.color.swatch
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = swatch,
        contentColor = ink,
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                ProgressRing(
                    progress = count / habit.dailyGoal.toFloat(),
                    modifier = Modifier.matchParentSize(),
                    color = ink,
                    trackColor = ink.copy(alpha = 0.14f),
                    strokeWidth = 8.dp,
                )
                Text("$count/${habit.dailyGoal}", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.detail_today),
                    style = MaterialTheme.typography.labelLarge,
                    color = ink.copy(alpha = 0.65f),
                )
                Text(
                    text = stringResource(R.string.habit_progress_today, count, habit.dailyGoal),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = onUndo,
                        enabled = count > 0,
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = ink.copy(alpha = 0.1f),
                                contentColor = ink,
                                disabledContainerColor = ink.copy(alpha = 0.05f),
                                disabledContentColor = ink.copy(alpha = 0.3f),
                            ),
                    ) {
                        Icon(
                            Icons.Rounded.Remove,
                            contentDescription = stringResource(R.string.detail_remove_check_in),
                        )
                    }
                    IconButton(
                        onClick = onCheckIn,
                        enabled = count < habit.dailyGoal,
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = ink,
                                contentColor = swatch,
                                disabledContainerColor = ink.copy(alpha = 0.3f),
                                disabledContentColor = swatch,
                            ),
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.detail_add_check_in),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stats(
    state: HabitDetailUiState,
    habit: Habit,
) {
    val accent = habit.color.swatch
    val stats = state.stats
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Rounded.LocalFireDepartment,
                value = stats.currentStreak.toString(),
                label = stringResource(R.string.detail_current_streak),
                accent = accent,
                onAccent = CairnPalette.Ink,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = Icons.Rounded.EmojiEvents,
                value = stats.bestStreak.toString(),
                label = stringResource(R.string.detail_best_streak),
                accent = accent,
                onAccent = CairnPalette.Ink,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                value = "${(stats.completionRate * 100).roundToInt()}%",
                label = stringResource(R.string.detail_rate),
                accent = accent,
                onAccent = CairnPalette.Ink,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = Icons.Rounded.DoneAll,
                value = stats.totalCheckIns.toString(),
                label = stringResource(R.string.detail_check_ins),
                accent = accent,
                onAccent = CairnPalette.Ink,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun History(
    cells: List<Float?>,
    habit: Habit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                stringResource(R.string.detail_history),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text =
                    pluralStringResource(
                        R.plurals.detail_history_weeks,
                        HabitDetailViewModel.HEATMAP_WEEKS,
                        HabitDetailViewModel.HEATMAP_WEEKS,
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Heatmap(cells = cells, color = habit.color.swatch, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            HeatmapLegend(color = habit.color.swatch, modifier = Modifier.align(Alignment.End))
        }
    }
}
