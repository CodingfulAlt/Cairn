package io.github.codingfulalt.cairn.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import io.github.codingfulalt.cairn.core.designsystem.component.Bar
import io.github.codingfulalt.cairn.core.designsystem.component.EmptyState
import io.github.codingfulalt.cairn.core.designsystem.component.HabitBadge
import io.github.codingfulalt.cairn.core.designsystem.component.Heatmap
import io.github.codingfulalt.cairn.core.designsystem.component.HeatmapLegend
import io.github.codingfulalt.cairn.core.designsystem.component.StatTile
import io.github.codingfulalt.cairn.core.designsystem.component.WeekBars
import io.github.codingfulalt.cairn.core.ui.currentLocale
import io.github.codingfulalt.cairn.core.ui.shortLabel
import kotlin.math.roundToInt

private val WIDE_LAYOUT = 840.dp

@Composable
fun InsightsScreen(
    onOpenHabit: (Long) -> Unit,
    contentPadding: PaddingValues,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsContent(state, onOpenHabit, contentPadding)
}

@Composable
internal fun InsightsContent(
    state: InsightsUiState,
    onOpenHabit: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val wide = maxWidth >= WIDE_LAYOUT
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = top,
                        bottom =
                            contentPadding.calculateBottomPadding() + 8.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    stringResource(R.string.insights_title),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = stringResource(R.string.insights_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                state.loading -> Unit
                !state.hasHabits ->
                    EmptyState(
                        title = stringResource(R.string.insights_empty_title),
                        body = stringResource(R.string.insights_empty_body),
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    )
                else -> {
                    StatGrid(state, columns = if (wide) 4 else 2)
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LastSevenDays(state, Modifier.weight(1f))
                            Activity(state, Modifier.weight(1f))
                        }
                    } else {
                        LastSevenDays(state)
                        Activity(state)
                    }
                    if (state.leaders.isNotEmpty()) Leaders(state, onOpenHabit)
                }
            }
        }
    }
}

@Composable
private fun StatGrid(
    state: InsightsUiState,
    columns: Int,
) {
    val tiles =
        listOf(
            Triple(
                Icons.Rounded.AutoAwesome,
                state.perfectDays.toString(),
                R.string.insights_perfect_days,
            ),
            Triple(
                Icons.AutoMirrored.Rounded.TrendingUp,
                "${(state.completionRate * 100).roundToInt()}%",
                R.string.insights_completion,
            ),
            Triple(
                Icons.Rounded.LocalFireDepartment,
                state.topStreak.toString(),
                R.string.insights_top_streak,
            ),
            Triple(
                Icons.Rounded.DoneAll,
                state.totalCheckIns.toString(),
                R.string.insights_check_ins,
            ),
        )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (icon, value, label) ->
                    StatTile(
                        icon = icon,
                        value = value,
                        label = stringResource(label),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun Card(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun LastSevenDays(
    state: InsightsUiState,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()
    Card(title = stringResource(R.string.insights_last_7_days), modifier = modifier) {
        WeekBars(
            bars =
                state.lastSevenDays.map { day ->
                    Bar(
                        label = day.date.dayOfWeek.shortLabel(locale),
                        value = day.ratio,
                        highlighted = day.date == state.today,
                    )
                },
        )
    }
}

@Composable
private fun Activity(
    state: InsightsUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        title = stringResource(R.string.insights_activity),
        subtitle =
            pluralStringResource(
                R.plurals.detail_history_weeks,
                InsightsViewModel.HEATMAP_WEEKS,
                InsightsViewModel.HEATMAP_WEEKS,
            ),
        modifier = modifier,
    ) {
        val color = MaterialTheme.colorScheme.primary
        Heatmap(cells = state.heatmap, color = color, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        HeatmapLegend(color = color, modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun Leaders(
    state: InsightsUiState,
    onOpenHabit: (Long) -> Unit,
) {
    Card(title = stringResource(R.string.insights_leaders)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            state.leaders.forEach { leader ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onOpenHabit(leader.habit.id) }
                            .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HabitBadge(leader.habit.icon, leader.habit.color, size = 38.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = leader.habit.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF8A4C),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text =
                            pluralStringResource(
                                R.plurals.days_count,
                                leader.streak,
                                leader.streak,
                            ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
