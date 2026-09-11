package io.github.codingfulalt.cairn.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.component.CheckInButton
import io.github.codingfulalt.cairn.core.designsystem.component.HabitBadge
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.model.Habit

/** Pending habits sit on a neutral card, finished ones fill up with their color. */
@Composable
fun HabitCard(
    habit: Habit,
    count: Int,
    streak: Int,
    onClick: () -> Unit,
    onCheckIn: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val done = count >= habit.dailyGoal
    val container by animateColorAsState(
        if (done) habit.color.swatch else MaterialTheme.colorScheme.surfaceContainer,
        label = "cardColor",
    )
    val content by animateColorAsState(
        if (done) CairnPalette.Ink else MaterialTheme.colorScheme.onSurface,
        label = "cardContent",
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HabitBadge(
                icon = habit.icon,
                color = habit.color,
                size = 48.dp,
                container = if (done) Color.White.copy(alpha = 0.55f) else habit.color.swatch,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text =
                            if (habit.dailyGoal > 1) {
                                stringResource(
                                    R.string.habit_progress_today,
                                    count,
                                    habit.dailyGoal,
                                )
                            } else {
                                scheduleLabel(habit.schedule)
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = content.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (streak > 0) {
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (done) CairnPalette.Ink else Color(0xFFFF8A4C),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = pluralStringResource(R.plurals.streak_days, streak, streak),
                            style = MaterialTheme.typography.labelMedium,
                            color = content.copy(alpha = 0.8f),
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            CheckInButton(
                count = count,
                goal = habit.dailyGoal,
                onCheckIn = onCheckIn,
                onUndo = onUndo,
                label = stringResource(R.string.habit_check_in, habit.name),
                contentColor = content,
                checkColor = if (done) habit.color.swatch else MaterialTheme.colorScheme.surface,
            )
        }
    }
}
