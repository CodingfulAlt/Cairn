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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

@Immutable
data class Stone(
    val color: Color,
    val placed: Boolean,
)

private const val MAX_STONES = 7

// one flat stone from the logo, as fractions of its bounding box
private val SLAB =
    floatArrayOf(
        0f,
        .65f,
        .05f,
        .18f,
        .29f,
        0f,
        .76f,
        0f,
        .97f,
        .24f,
        1f,
        .71f,
        .92f,
        .94f,
        .08f,
        1f,
    )
private val SLAB_SHADE = floatArrayOf(.57f, .53f, .97f, .24f, 1f, .71f, .92f, .94f, .53f, 1f)
private val SLAB_STRIPE = floatArrayOf(.19f, .4f, .49f, .34f, .49f, .6f, .19f, .66f)
private val SHIFT = floatArrayOf(0f, -.045f, .07f, -.04f, .05f, -.03f, .04f)

/**
 * The stack on the Today screen, one flat stone per habit, bottom first.
 * Placed stones drop in with a bounce, the rest are dashed outlines.
 */
@Composable
fun CairnStack(
    stones: List<Stone>,
    modifier: Modifier = Modifier,
    outlineColor: Color = LocalContentColor.current.copy(alpha = 0.35f),
    shadeColor: Color = Color.Black.copy(alpha = 0.18f),
    stripeColor: Color? = null,
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
        val gap = size.height * 0.03f
        val widths =
            List(count) { i ->
                val t = if (count == 1) 0f else i / (count - 1f)
                size.width * (0.96f - 0.28f * t)
            }
        val heights = widths.map { it * 0.24f }
        val scale = ((size.height - gap * (count - 1)) / heights.sum()).coerceAtMost(1f)
        val dash = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
        val outline = Stroke(width = 1.5.dp.toPx(), pathEffect = dash)

        var bottom = size.height
        visible.forEachIndexed { i, stone ->
            val w = widths[i] * scale.coerceAtLeast(0.85f)
            val h = heights[i] * scale
            val left = (size.width - w) / 2f + size.width * SHIFT[i % SHIFT.size]
            val top = bottom - h
            val progress = placed[i]
            val body = slab(SLAB, left, top, w, h)
            if (progress < 1f) {
                drawPath(
                    body,
                    outlineColor,
                    alpha = (1f - progress).coerceIn(0f, 1f),
                    style = outline,
                )
            }
            if (progress > 0f) {
                val alpha = progress.coerceIn(0f, 1f)
                translate(top = -(1f - progress) * size.height * 0.12f) {
                    drawPath(body, stone.color, alpha = alpha)
                    if (i == 1 && stripeColor != null) {
                        drawPath(slab(SLAB_STRIPE, left, top, w, h), stripeColor, alpha = alpha)
                    }
                    drawPath(slab(SLAB_SHADE, left, top, w, h), shadeColor, alpha = alpha)
                }
            }
            bottom = top - gap
        }
    }
}

private fun slab(
    points: FloatArray,
    left: Float,
    top: Float,
    w: Float,
    h: Float,
) = Path().apply {
    for (i in points.indices step 2) {
        val x = left + points[i] * w
        val y = top + points[i + 1] * h
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
