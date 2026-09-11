package io.github.codingfulalt.cairn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.core.designsystem.component.CairnStack
import io.github.codingfulalt.cairn.core.designsystem.component.Stone
import io.github.codingfulalt.cairn.navigation.TopLevelDestination

private val LOGO_STONES = List(3) { Stone(Color.Unspecified, placed = true) }

@Composable
fun CairnNavRail(
    current: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .width(92.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.Vertical + WindowInsetsSides.Start,
                    ),
                ).padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CairnStack(
            stones = LOGO_STONES.map { it.copy(color = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier.size(width = 34.dp, height = 30.dp),
        )
        Spacer(Modifier.height(18.dp))
        AddButton(onClick = onAdd, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(22.dp))
        TopLevelDestination.entries.forEach { destination ->
            RailItem(
                destination,
                selected = destination == current,
                onClick = { onSelect(destination) },
            )
        }
    }
}

@Composable
private fun RailItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val indicator by animateColorAsState(
        if (selected) colors.primaryContainer else Color.Transparent,
        label = "indicator",
    )
    val tint by animateColorAsState(
        if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        label = "tint",
    )
    Column(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.medium)
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 58.dp, height = 34.dp)
                    .clip(CircleShape)
                    .background(indicator),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (selected) destination.selectedIcon else destination.icon,
                contentDescription = null,
                tint = tint,
            )
        }
        Text(
            text = stringResource(destination.label),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
