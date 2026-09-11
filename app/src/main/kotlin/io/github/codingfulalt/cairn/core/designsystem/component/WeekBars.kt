package io.github.codingfulalt.cairn.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Bar(
    val label: String,
    val value: Float,
    val highlighted: Boolean = false,
)

@Composable
fun WeekBars(
    bars: List<Bar>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    height: Dp = 132.dp,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        bars.forEach { bar ->
            val fraction by animateFloatAsState(
                targetValue = bar.value.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 700),
                label = "bar",
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(height)
                            .clip(RoundedCornerShape(12.dp))
                            .background(trackColor),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (bar.highlighted) highlightColor else barColor),
                        )
                    }
                }
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (bar.highlighted) FontWeight.Bold else FontWeight.Medium,
                    color =
                        if (bar.highlighted) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
