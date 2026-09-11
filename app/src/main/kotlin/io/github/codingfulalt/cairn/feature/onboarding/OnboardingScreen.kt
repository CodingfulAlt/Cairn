package io.github.codingfulalt.cairn.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.component.CairnStack
import io.github.codingfulalt.cairn.core.designsystem.component.Stone
import io.github.codingfulalt.cairn.core.designsystem.icon.imageVector
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.model.HabitColor
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val titles = HabitSuggestion.entries.associateWith { stringResource(it.title) }
    OnboardingContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onToggle = viewModel::toggle,
        onStart = { viewModel.finish { titles.getValue(it) } },
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OnboardingContent(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onToggle: (HabitSuggestion) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
            Spacer(Modifier.height(28.dp))
            DroppingStones(
                Modifier.align(Alignment.CenterHorizontally).size(width = 190.dp, height = 170.dp),
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.onboarding_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))
            Text(
                stringResource(R.string.onboarding_name),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                placeholder = { Text(stringResource(R.string.onboarding_name_placeholder)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.onboarding_suggestions),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HabitSuggestion.entries.forEach { suggestion ->
                    FilterChip(
                        selected = suggestion in state.selected,
                        onClick = { onToggle(suggestion) },
                        label = { Text(stringResource(suggestion.title)) },
                        leadingIcon = {
                            Icon(
                                suggestion.icon.imageVector,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        shape = CircleShape,
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = suggestion.color.swatch,
                                selectedLabelColor = CairnPalette.Ink,
                                selectedLeadingIconColor = CairnPalette.Ink,
                            ),
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
            Button(
                onClick = onStart,
                enabled = !state.saving,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().height(58.dp),
            ) {
                Text(
                    stringResource(R.string.onboarding_start),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DroppingStones(modifier: Modifier = Modifier) {
    val colors =
        remember {
            listOf(
                HabitColor.Moss,
                HabitColor.Sky,
                HabitColor.Lavender,
                HabitColor.Peach,
            ).map { it.swatch }
        }
    var placed by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(colors.size) {
            delay(380)
            placed++
        }
    }
    CairnStack(
        stones = colors.mapIndexed { index, color -> Stone(color, placed = index < placed) },
        modifier = modifier,
    )
}
