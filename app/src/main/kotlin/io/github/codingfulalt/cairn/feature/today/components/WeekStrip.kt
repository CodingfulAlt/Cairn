package io.github.codingfulalt.cairn.feature.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.component.ProgressRing
import io.github.codingfulalt.cairn.core.ui.currentLocale
import io.github.codingfulalt.cairn.core.ui.monthYear
import io.github.codingfulalt.cairn.core.ui.shortLabel
import io.github.codingfulalt.cairn.feature.today.WeekDay
import java.time.LocalDate
import java.util.Locale

@Composable
fun WeekStrip(
    days: List<WeekDay>,
    selected: LocalDate,
    canGoNext: Boolean,
    onSelect: (LocalDate) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = selected.monthYear(locale),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.today_previous_week),
                )
            }
            IconButton(onClick = onNext, enabled = canGoNext) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.today_next_week),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            days.forEach { day ->
                DayChip(
                    day = day,
                    selected = day.date == selected,
                    locale = locale,
                    onClick = { onSelect(day.date) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DayChip(
    day: WeekDay,
    selected: Boolean,
    locale: Locale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    val container by animateColorAsState(
        if (selected) colors.primary else colors.surfaceContainer,
        label = "chip",
    )
    val content by animateColorAsState(
        if (selected) colors.onPrimary else colors.onSurface,
        label = "chipContent",
    )
    val outline =
        if (day.isToday &&
            !selected
        ) {
            Modifier.border(1.5.dp, colors.primary, shape)
        } else {
            Modifier
        }

    Column(
        modifier =
            modifier
                .clip(shape)
                .background(container)
                .then(outline)
                .selectable(
                    selected = selected,
                    enabled = !day.isFuture,
                    role = Role.Tab,
                    onClick = onClick,
                ).alpha(if (day.isFuture) 0.4f else 1f)
                .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.date.dayOfWeek.shortLabel(locale),
            style = MaterialTheme.typography.labelSmall,
            color = content.copy(alpha = 0.75f),
            maxLines = 1,
        )
        Spacer(Modifier.height(6.dp))
        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            ProgressRing(
                progress = day.ratio,
                modifier = Modifier.matchParentSize(),
                color = content,
                trackColor = content.copy(alpha = 0.14f),
                strokeWidth = 2.5.dp,
            )
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = content,
            )
        }
    }
}
