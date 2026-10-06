package io.github.codingfulalt.cairn.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import kotlin.math.min

// same shapes as the launcher icon, in a 100 x 100 box
private val BOTTOM =
    floatArrayOf(12f, 69f, 16f, 61f, 34f, 58f, 70f, 58f, 86f, 62f, 88f, 70f, 82f, 74f, 18f, 75f)
private val BOTTOM_SHADE = floatArrayOf(55f, 67f, 86f, 62f, 88f, 70f, 82f, 74f, 52f, 75f)
private val MIDDLE =
    floatArrayOf(18f, 52f, 22f, 44f, 40f, 41f, 64f, 41.5f, 74f, 46f, 73f, 53f, 66f, 56f, 22f, 56f)
private val MIDDLE_SHADE = floatArrayOf(48f, 50f, 74f, 46f, 73f, 53f, 66f, 56f, 46f, 56f)
private val STRIPE = floatArrayOf(28.41f, 46.95f, 45.38f, 46.06f, 45.59f, 50.05f, 28.62f, 50.94f)
private val TOP =
    floatArrayOf(38f, 35f, 42f, 28f, 56f, 25f, 72f, 26f, 80f, 30f, 78f, 36f, 70f, 38.5f, 42f, 38.5f)
private val TOP_SHADE = floatArrayOf(60f, 33f, 80f, 30f, 78f, 36f, 70f, 38.5f, 58f, 38.5f)

/** The Cairn logo: three flat stones with a red trail blaze on the middle one. */
@Composable
fun SlateMark(
    modifier: Modifier = Modifier,
    stone: Color = LocalContentColor.current,
    shade: Color = Color.Black.copy(alpha = 0.22f),
    stripe: Color = CairnPalette.Blaze,
) {
    Canvas(modifier) {
        val scale = min(size.width, size.height) / 100f
        val dx = (size.width - 100f * scale) / 2f
        val dy = (size.height - 100f * scale) / 2f

        fun shape(points: FloatArray) =
            Path().apply {
                for (i in points.indices step 2) {
                    val x = dx + points[i] * scale
                    val y = dy + points[i + 1] * scale
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

        drawPath(shape(BOTTOM), stone)
        drawPath(shape(BOTTOM_SHADE), shade)
        drawPath(shape(MIDDLE), stone)
        drawPath(shape(STRIPE), stripe)
        drawPath(shape(MIDDLE_SHADE), shade)
        drawPath(shape(TOP), stone)
        drawPath(shape(TOP_SHADE), shade)
    }
}
