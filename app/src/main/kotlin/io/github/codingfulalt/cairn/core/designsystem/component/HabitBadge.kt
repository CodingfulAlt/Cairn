package io.github.codingfulalt.cairn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.core.designsystem.icon.imageVector
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon

@Composable
fun HabitBadge(
    icon: HabitIcon,
    color: HabitColor,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    container: Color = color.swatch,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon.imageVector,
            contentDescription = null,
            tint = CairnPalette.Ink,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}
