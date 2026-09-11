package io.github.codingfulalt.cairn.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.R

private val HEAT_LEVELS = listOf(0.3f, 0.5f, 0.75f, 1f)

private fun heatAlpha(value: Float): Float =
    when {
        value <= 0.25f -> HEAT_LEVELS[0]
        value <= 0.5f -> HEAT_LEVELS[1]
        value <= 0.75f -> HEAT_LEVELS[2]
        else -> HEAT_LEVELS[3]
    }

/**
 * GitHub style activity grid. [cells] is column major: 7 values per week, Monday first.
 * null means the day is in the future and gets skipped.
 */
@Composable
fun Heatmap(
    cells: List<Float?>,
    color: Color,
    modifier: Modifier = Modifier,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    maxCellSize: Dp = 18.dp,
    gap: Dp = 4.dp,
) {
    val weeks = (cells.size + 6) / 7
    if (weeks == 0) return
    BoxWithConstraints(modifier) {
        val cell = ((maxWidth - gap * (weeks - 1)) / weeks).coerceAtMost(maxCellSize)
        Canvas(
            Modifier
                .width(cell * weeks + gap * (weeks - 1))
                .height(cell * 7 + gap * 6),
        ) {
            val cellPx = cell.toPx()
            val gapPx = gap.toPx()
            val radius = CornerRadius(cellPx * 0.28f)
            cells.forEachIndexed { index, value ->
                if (value == null) return@forEachIndexed
                val x = (index / 7) * (cellPx + gapPx)
                val y = (index % 7) * (cellPx + gapPx)
                drawRoundRect(
                    color = if (value <= 0f) emptyColor else color.copy(alpha = heatAlpha(value)),
                    topLeft = Offset(x, y),
                    size = Size(cellPx, cellPx),
                    cornerRadius = radius,
                )
            }
        }
    }
}

@Composable
fun HeatmapLegend(
    color: Color,
    modifier: Modifier = Modifier,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.legend_less),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(2.dp))
        val shape = RoundedCornerShape(3.dp)
        Spacer(Modifier.size(10.dp).background(emptyColor, shape))
        HEAT_LEVELS.forEach { alpha ->
            Spacer(Modifier.size(10.dp).background(color.copy(alpha = alpha), shape))
        }
        Spacer(Modifier.width(2.dp))
        Text(
            text = stringResource(R.string.legend_more),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
