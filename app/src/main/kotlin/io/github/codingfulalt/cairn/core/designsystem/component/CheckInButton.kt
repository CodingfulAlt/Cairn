package io.github.codingfulalt.cairn.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.R

/**
 * Tap adds a check-in, long press takes one back.
 * Single-goal habits also toggle off with a second tap.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckInButton(
    count: Int,
    goal: Int,
    onCheckIn: () -> Unit,
    onUndo: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    checkColor: Color = MaterialTheme.colorScheme.surface,
    size: Dp = 52.dp,
) {
    val done = count >= goal
    val haptics = LocalHapticFeedback.current
    val fill by animateColorAsState(if (done) contentColor else Color.Transparent, label = "fill")
    val scale by animateFloatAsState(
        targetValue = if (done) 1f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale",
    )
    val undoLabel = stringResource(R.string.habit_undo_check_in)

    Box(
        modifier =
            modifier
                .size(size)
                .scale(scale)
                .clip(CircleShape)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = label,
                    onLongClickLabel = undoLabel,
                    onLongClick = {
                        if (count > 0) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUndo()
                        }
                    },
                    onClick = {
                        when {
                            !done -> {
                                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                onCheckIn()
                            }
                            goal == 1 -> {
                                haptics.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                onUndo()
                            }
                        }
                    },
                ).semantics {
                    contentDescription = label
                    stateDescription = "$count/$goal"
                },
        contentAlignment = Alignment.Center,
    ) {
        ProgressRing(
            progress = count / goal.toFloat(),
            modifier = Modifier.matchParentSize().padding(2.dp),
            color = contentColor,
            trackColor = contentColor.copy(alpha = 0.15f),
            strokeWidth = 3.dp,
        )
        Box(
            Modifier
                .matchParentSize()
                .padding(7.dp)
                .clip(CircleShape)
                .background(fill),
        )
        AnimatedContent(
            targetState = done,
            transitionSpec = { (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith fadeOut() },
            label = "checkContent",
        ) { isDone ->
            when {
                isDone -> Icon(Icons.Rounded.Check, contentDescription = null, tint = checkColor)
                goal > 1 ->
                    Text(
                        text = "$count/$goal",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                    )
                else ->
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.7f),
                    )
            }
        }
    }
}
