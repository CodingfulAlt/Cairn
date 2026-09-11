package io.github.codingfulalt.cairn.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Immutable
data class Stone(
    val color: Color,
    val placed: Boolean,
)

private const val MAX_STONES = 7

/**
 * The little cairn on the Today screen. Stones are listed bottom first,
 * placed ones drop in with a bounce, the rest are dashed outlines.
 */
@Composable
fun CairnStack(
    stones: List<Stone>,
    modifier: Modifier = Modifier,
    outlineColor: Color = LocalContentColor.current.copy(alpha = 0.35f),
) {
    val visible = stones.take(MAX_STONES)
    val placed =
        visible.mapIndexed { index, stone ->
            key(index) {
                animateFloatAsState(
                    targetValue = if (stone.placed) 1f else 0f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    label = "stone$index",
                ).value
            }
        }

    Canvas(modifier) {
        if (visible.isEmpty()) return@Canvas
        val count = visible.size
        val gap = size.height * 0.035f
        val widths =
            List(count) { i ->
                val t = if (count == 1) 0f else i / (count - 1f)
                size.width * (0.92f - 0.52f * t)
            }
        val rawHeights = widths.map { it * 0.34f }
        val scale = ((size.height - gap * (count - 1)) / rawHeights.sum()).coerceAtMost(1f)
        val heights = rawHeights.map { it * scale }
        val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
        val strokeWidth = 1.5.dp.toPx()

        var bottom = size.height
        visible.forEachIndexed { i, stone ->
            val w = widths[i] * scale.coerceAtLeast(0.75f)
            val h = heights[i]
            val cx = size.width / 2 + size.width * 0.04f * sin(i * 2.3f)
            val top = bottom - h
            val progress = placed[i]
            val drop = (1f - progress) * size.height * 0.12f
            rotate(degrees = 4f * sin(i * 1.7f + 1f), pivot = Offset(cx, top + h / 2)) {
                if (progress < 1f) {
                    drawOval(
                        color = outlineColor,
                        topLeft = Offset(cx - w / 2, top),
                        size = Size(w, h),
                        alpha = 1f - progress,
                        style = Stroke(width = strokeWidth, pathEffect = dash),
                    )
                }
                if (progress > 0f) {
                    drawOval(
                        color = stone.color,
                        topLeft = Offset(cx - w / 2, top - drop),
                        size = Size(w, h),
                        alpha = progress.coerceIn(0f, 1f),
                    )
                }
            }
            bottom = top - gap
        }
    }
}
