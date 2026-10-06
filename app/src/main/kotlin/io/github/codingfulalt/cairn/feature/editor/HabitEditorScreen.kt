package io.github.codingfulalt.cairn.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.icon.imageVector
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnPalette
import io.github.codingfulalt.cairn.core.designsystem.theme.swatch
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.HabitColor
import io.github.codingfulalt.cairn.core.model.HabitIcon
import io.github.codingfulalt.cairn.core.ui.HabitCard
import io.github.codingfulalt.cairn.core.ui.NotificationsBlockedBanner
import io.github.codingfulalt.cairn.core.ui.TimePickerDialog
import io.github.codingfulalt.cairn.core.ui.currentLocale
import io.github.codingfulalt.cairn.core.ui.formatTime
import io.github.codingfulalt.cairn.core.ui.label
import io.github.codingfulalt.cairn.core.ui.rememberNotificationPermission
import io.github.codingfulalt.cairn.core.ui.shortLabel
import io.github.codingfulalt.cairn.core.ui.weekDays
import java.time.LocalDate

@Composable
fun HabitEditorScreen(
    onClose: () -> Unit,
    viewModel: HabitEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onClose()
    }
    HabitEditorContent(state = state, viewModel = viewModel, onClose = onClose)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitEditorContent(
    state: HabitEditorUiState,
    viewModel: HabitEditorViewModel,
    onClose: () -> Unit,
) {
    val titleRes = if (state.isNew) R.string.editor_title_new else R.string.editor_title_edit
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.action_close),
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.saving && !state.loading,
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Preview(state)
                NameFields(state, viewModel)
                IconPicker(
                    selected = state.icon,
                    color = state.color,
                    onSelect = viewModel::onIconChange,
                )
                ColorPicker(selected = state.color, onSelect = viewModel::onColorChange)
                GoalStepper(goal = state.dailyGoal, onChange = viewModel::changeGoal)
                SchedulePicker(state, viewModel)
                ReminderSection(state, viewModel)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun Preview(state: HabitEditorUiState) {
    Section(stringResource(R.string.editor_preview)) {
        HabitCard(
            habit =
                Habit(
                    name = state.name.ifBlank { stringResource(R.string.editor_name_placeholder) },
                    icon = state.icon,
                    color = state.color,
                    dailyGoal = state.dailyGoal,
                    schedule = state.schedule,
                    createdOn = LocalDate.MIN,
                ),
            count = 0,
            streak = 0,
            onClick = {},
            onCheckIn = {},
            onUndo = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NameFields(
    state: HabitEditorUiState,
    viewModel: HabitEditorViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val showNameError = state.showErrors && state.nameError
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text(stringResource(R.string.editor_name_label)) },
            placeholder = { Text(stringResource(R.string.editor_name_placeholder)) },
            isError = showNameError,
            supportingText = {
                if (showNameError) {
                    Text(stringResource(R.string.editor_name_error))
                } else {
                    Text("${state.name.length}/${Habit.MAX_NAME_LENGTH}")
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text(stringResource(R.string.editor_note_label)) },
            placeholder = { Text(stringResource(R.string.editor_note_placeholder)) },
            maxLines = 3,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(
    selected: HabitIcon,
    color: HabitColor,
    onSelect: (HabitIcon) -> Unit,
) {
    val icons = HabitIcon.entries
    val colors = MaterialTheme.colorScheme
    Section(stringResource(R.string.editor_icon)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            icons.forEachIndexed { index, icon ->
                val isSelected = icon == selected
                val description = stringResource(R.string.editor_icon_option, index + 1, icons.size)
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) color.swatch else colors.surfaceContainerHigh,
                            ).selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onSelect(icon) },
                            ).semantics { contentDescription = description },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon.imageVector,
                        contentDescription = null,
                        tint = if (isSelected) CairnPalette.Ink else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPicker(
    selected: HabitColor,
    onSelect: (HabitColor) -> Unit,
) {
    val ring = MaterialTheme.colorScheme.onSurface
    Section(stringResource(R.string.editor_color)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HabitColor.entries.forEach { color ->
                val isSelected = color == selected
                val description = color.label()
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color.swatch)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) ring else Color.Transparent,
                                shape = CircleShape,
                            ).selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onSelect(color) },
                            ).semantics { contentDescription = description },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = CairnPalette.Ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalStepper(
    goal: Int,
    onChange: (Int) -> Unit,
) {
    Section(stringResource(R.string.editor_goal)) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(onClick = { onChange(-1) }, enabled = goal > 1) {
                    Icon(
                        Icons.Rounded.Remove,
                        contentDescription = stringResource(R.string.editor_goal_decrease),
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(goal.toString(), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = pluralStringResource(R.plurals.goal_per_day, goal, goal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalIconButton(
                    onClick = { onChange(1) },
                    enabled =
                        goal < Habit.MAX_DAILY_GOAL,
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.editor_goal_increase),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SchedulePicker(
    state: HabitEditorUiState,
    viewModel: HabitEditorViewModel,
) {
    val locale = currentLocale()
    val colors = MaterialTheme.colorScheme
    Section(stringResource(R.string.editor_schedule)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                R.string.schedule_every_day to Habit.EVERY_DAY,
                R.string.schedule_weekdays to Habit.WEEKDAYS,
                R.string.schedule_weekends to Habit.WEEKENDS,
            ).forEach { (label, days) ->
                FilterChip(
                    selected = state.schedule == days,
                    onClick = { viewModel.setSchedule(days) },
                    label = { Text(stringResource(label)) },
                    shape = CircleShape,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekDays(locale).forEach { day ->
                val on = day in state.schedule
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(if (on) colors.primary else colors.surfaceContainerHigh)
                            .toggleable(
                                value = on,
                                role = Role.Checkbox,
                                onValueChange = { viewModel.toggleDay(day) },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day.shortLabel(locale),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (on) colors.onPrimary else colors.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        if (state.showErrors && state.scheduleError) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.editor_schedule_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ReminderSection(
    state: HabitEditorUiState,
    viewModel: HabitEditorViewModel,
) {
    val permission = rememberNotificationPermission()
    var pickingTime by rememberSaveable { mutableStateOf(false) }

    Section(stringResource(R.string.editor_reminder)) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.editor_reminder_body),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setReminderEnabled(enabled)
                            if (enabled) permission.request()
                        },
                    )
                }
                if (state.reminderEnabled) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { pickingTime = true }, shape = CircleShape) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(formatTime(state.reminderTime))
                    }
                }
            }
        }
        if (state.reminderEnabled && permission.denied) {
            Spacer(Modifier.height(10.dp))
            NotificationsBlockedBanner(onOpenSettings = permission.openSettings)
        }
    }

    if (pickingTime) {
        TimePickerDialog(
            initial = state.reminderTime,
            onConfirm = {
                viewModel.setReminderTime(it)
                pickingTime = false
            },
            onDismiss = { pickingTime = false },
        )
    }
}
