package io.github.codingfulalt.cairn.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Piece(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val spin: Float,
    val round: Boolean,
    val color: Color,
)

private const val DURATION_MS = 1800
private const val PIECES = 70

/** Fires a one-shot burst every time [trigger] changes to a new non-zero value. */
@Composable
fun ConfettiBurst(
    trigger: Int,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    if (trigger == 0 || colors.isEmpty()) return
    val progress = remember(trigger) { Animatable(0f) }
    val pieces =
        remember(trigger) {
            val random = Random(trigger)
            List(PIECES) {
                Piece(
                    angle = Math.toRadians(-150.0 + random.nextDouble() * 120.0).toFloat(),
                    speed = 0.55f + random.nextFloat() * 0.65f,
                    size = 0.012f + random.nextFloat() * 0.012f,
                    spin = random.nextFloat() * 720f - 360f,
                    round = random.nextBoolean(),
                    color = colors[random.nextInt(colors.size)],
                )
            }
        }
    LaunchedEffect(trigger) {
        progress.animateTo(1f, tween(DURATION_MS, easing = LinearEasing))
    }

    Canvas(modifier) {
        val p = progress.value
        if (p >= 1f) return@Canvas
        val t = p * DURATION_MS / 1000f
        val gravity = size.height * 1.4f
        val origin = Offset(size.width / 2, size.height * 0.28f)
        val fade = if (p > 0.7f) (1f - p) / 0.3f else 1f
        pieces.forEach { piece ->
            val velocity = piece.speed * size.height
            val x = origin.x + cos(piece.angle) * velocity * t
            val y = origin.y + sin(piece.angle) * velocity * t + 0.5f * gravity * t * t
            val side = piece.size * size.width
            if (piece.round) {
                drawCircle(piece.color, radius = side / 2, center = Offset(x, y), alpha = fade)
            } else {
                rotate(piece.spin * t, pivot = Offset(x, y)) {
                    drawRect(
                        color = piece.color,
                        topLeft = Offset(x - side / 2, y - side / 4),
                        size = Size(side, side / 2),
                        alpha = fade,
                    )
                }
            }
        }
    }
}
