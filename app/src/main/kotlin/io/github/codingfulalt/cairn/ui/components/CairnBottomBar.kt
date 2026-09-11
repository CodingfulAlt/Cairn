package io.github.codingfulalt.cairn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnTheme
import io.github.codingfulalt.cairn.navigation.TopLevelDestination

private val BAR_HEIGHT = 68.dp
private val BAR_MARGIN = 14.dp

/** Space a screen should leave at the bottom so its last item isn't hidden behind the bar. */
val BOTTOM_BAR_CLEARANCE = BAR_HEIGHT + BAR_MARGIN * 2

@Composable
fun CairnBottomBar(
    current: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CairnTheme.colors
    val destinations = TopLevelDestination.entries
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = BAR_MARGIN),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier =
                Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .height(BAR_HEIGHT)
                    .shadow(
                        elevation = 18.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black,
                    ).clip(CircleShape)
                    .background(colors.navBar)
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            destinations.take(2).forEach { destination ->
                BarItem(
                    destination,
                    selected = destination == current,
                    onClick = { onSelect(destination) },
                )
            }
            AddButton(onClick = onAdd, modifier = Modifier.size(52.dp))
            destinations.drop(2).forEach { destination ->
                BarItem(
                    destination,
                    selected = destination == current,
                    onClick = { onSelect(destination) },
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = CairnTheme.colors
    val tint by animateColorAsState(
        if (selected) colors.navBarSelected else colors.navBarContent,
        label = "tint",
    )
    val dot by animateDpAsState(if (selected) 5.dp else 0.dp, label = "dot")
    Column(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) destination.selectedIcon else destination.icon,
            contentDescription = stringResource(destination.label),
            tint = tint,
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .size(dot)
                .clip(CircleShape)
                .background(colors.navBarSelected),
        )
    }
}

@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CairnTheme.colors
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(colors.accent)
                .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.action_new_habit),
            tint = colors.onAccent,
        )
    }
}
