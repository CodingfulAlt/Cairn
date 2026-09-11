package io.github.codingfulalt.cairn.feature.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.component.CairnStack
import io.github.codingfulalt.cairn.core.designsystem.component.Stone
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnTheme
import io.github.codingfulalt.cairn.core.domain.DayProgress
import io.github.codingfulalt.cairn.core.ui.currentLocale
import io.github.codingfulalt.cairn.core.ui.fullDate

private const val MAX_SEGMENTS = 12

@Composable
fun TodayHero(
    progress: DayProgress,
    stones: List<Stone>,
    isViewingToday: Boolean,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ink = CairnPalette.Ink
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = CairnTheme.colors.accent,
        contentColor = ink,
    ) {
        Row(
            modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 22.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text =
                        if (isViewingToday) {
                            stringResource(R.string.today_stack_label)
                        } else {
                            progress.date.fullDate(currentLocale())
                        },
                    style = MaterialTheme.typography.labelLarge,
                    color = ink.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(4.dp))
                AnimatedContent(
                    targetState = progress.completed to progress.scheduled,
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn()) togetherWith
                            (slideOutVertically { -it / 2 } + fadeOut())
                    },
                    label = "count",
                ) { (completed, scheduled) ->
                    Text(
                        text = stringResource(R.string.today_stack_count, completed, scheduled),
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
                Text(
                    text = heroCaption(progress, isViewingToday),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ink.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(16.dp))
                SegmentBar(completed = progress.completed, total = progress.scheduled, color = ink)
                if (!isViewingToday) {
                    Spacer(Modifier.height(10.dp))
                    TextButton(
                        onClick = onJumpToToday,
                        colors = ButtonDefaults.textButtonColors(contentColor = ink),
                        contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
                    ) {
                        Icon(
                            Icons.Rounded.Today,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.today_jump_to_today))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            CairnStack(
                stones = stones,
                modifier = Modifier.size(width = 112.dp, height = 124.dp),
                outlineColor = ink.copy(alpha = 0.35f),
            )
        }
    }
}

@Composable
private fun heroCaption(
    progress: DayProgress,
    isViewingToday: Boolean,
): String =
    when {
        progress.scheduled == 0 -> stringResource(R.string.today_rest_body)
        progress.isPerfect ->
            stringResource(R.string.today_perfect_title) + ". " +
                stringResource(R.string.today_perfect_body)
        isViewingToday -> stringResource(R.string.today_stack_caption)
        else -> stringResource(R.string.today_stack_caption_past)
    }

@Composable
private fun SegmentBar(
    completed: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (total == 0) return
    if (total <= MAX_SEGMENTS) {
        Row(
            modifier = modifier.fillMaxWidth().height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(total) { index ->
                val segment by animateColorAsState(
                    if (index < completed) color else color.copy(alpha = 0.16f),
                    label = "segment",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(segment),
                )
            }
        }
    } else {
        val fraction by animateFloatAsState(completed / total.toFloat(), label = "fraction")
        Box(
            modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.16f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
