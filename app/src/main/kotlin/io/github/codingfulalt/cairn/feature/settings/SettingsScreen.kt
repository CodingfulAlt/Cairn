package io.github.codingfulalt.cairn.feature.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.codingfulalt.cairn.BuildConfig
import io.github.codingfulalt.cairn.R
import io.github.codingfulalt.cairn.core.designsystem.theme.CairnTheme
import io.github.codingfulalt.cairn.core.model.Habit
import io.github.codingfulalt.cairn.core.model.ThemeMode
import io.github.codingfulalt.cairn.core.model.UserPreferences
import io.github.codingfulalt.cairn.core.ui.NotificationsBlockedBanner
import io.github.codingfulalt.cairn.core.ui.TimePickerDialog
import io.github.codingfulalt.cairn.core.ui.formatTime
import io.github.codingfulalt.cairn.core.ui.rememberNotificationPermission

private const val SOURCE_URL = "https://github.com/CodingfulAlt"

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.loading) return
    SettingsContent(
        preferences = state.preferences,
        onThemeChange = viewModel::setThemeMode,
        onNameChange = viewModel::setUserName,
        onSummaryChange = viewModel::setDailySummary,
        onErase = viewModel::eraseAllData,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun SettingsContent(
    preferences: UserPreferences,
    onThemeChange: (ThemeMode) -> Unit,
    onNameChange: (String) -> Unit,
    onSummaryChange: (Boolean, java.time.LocalTime) -> Unit,
    onErase: () -> Unit,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val permission = rememberNotificationPermission()
    var editingName by rememberSaveable { mutableStateOf(false) }
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    var confirmErase by rememberSaveable { mutableStateOf(false) }
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = top,
                        bottom =
                            contentPadding.calculateBottomPadding() + 8.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
            )

            ProfileCard(name = preferences.userName, onEdit = { editingName = true })

            Group(stringResource(R.string.settings_appearance)) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.settings_theme),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(12.dp))
                    ThemeSelector(selected = preferences.themeMode, onSelect = onThemeChange)
                }
            }

            Group(stringResource(R.string.settings_notifications)) {
                SettingRow(
                    icon = Icons.Rounded.NightsStay,
                    title = stringResource(R.string.settings_summary),
                    subtitle =
                        stringResource(
                            R.string.settings_summary_body,
                            formatTime(preferences.dailySummaryTime),
                        ),
                    onClick = { pickingTime = true },
                    trailing = {
                        Switch(
                            checked = preferences.dailySummaryEnabled,
                            onCheckedChange = { enabled ->
                                onSummaryChange(enabled, preferences.dailySummaryTime)
                                if (enabled) permission.request()
                            },
                        )
                    },
                )
                if (preferences.dailySummaryEnabled && permission.denied) {
                    NotificationsBlockedBanner(
                        onOpenSettings = permission.openSettings,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingRow(
                        icon = Icons.Rounded.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = stringResource(R.string.settings_language_body),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APP_LOCALE_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                ),
                            )
                        },
                    )
                }
            }

            Group(stringResource(R.string.settings_data)) {
                SettingRow(
                    icon = Icons.Rounded.DeleteForever,
                    title = stringResource(R.string.settings_erase),
                    subtitle = stringResource(R.string.settings_erase_body),
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { confirmErase = true },
                )
            }

            Group(stringResource(R.string.settings_about)) {
                SettingRow(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.app_name),
                    subtitle = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(
                    icon = Icons.Rounded.Code,
                    title = stringResource(R.string.settings_source),
                    subtitle = stringResource(R.string.settings_source_body),
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, SOURCE_URL.toUri()),
                        )
                    },
                    trailing = {
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }

    if (editingName) {
        NameDialog(
            initial = preferences.userName,
            onConfirm = {
                onNameChange(it)
                editingName = false
            },
            onDismiss = { editingName = false },
        )
    }
    if (pickingTime) {
        TimePickerDialog(
            initial = preferences.dailySummaryTime,
            onConfirm = {
                onSummaryChange(true, it)
                permission.request()
                pickingTime = false
            },
            onDismiss = { pickingTime = false },
        )
    }
    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text(stringResource(R.string.settings_erase_title)) },
            text = { Text(stringResource(R.string.settings_erase_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmErase = false
                        onErase()
                    },
                ) {
                    Text(
                        stringResource(R.string.settings_erase_action),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmErase = false },
                ) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun ProfileCard(
    name: String,
    onEdit: () -> Unit,
) {
    val colors = CairnTheme.colors
    Surface(
        onClick = onEdit,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onAccent,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_profile),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = name.ifBlank { stringResource(R.string.settings_name_empty) },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.action_edit))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val options =
        listOf(
            Triple(ThemeMode.System, Icons.Rounded.BrightnessAuto, R.string.theme_system),
            Triple(ThemeMode.Light, Icons.Rounded.LightMode, R.string.theme_light),
            Triple(ThemeMode.Dark, Icons.Rounded.DarkMode, R.string.theme_dark),
        )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, icon, label) ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
            ) {
                Text(stringResource(label), maxLines = 1)
            }
        }
    }
}

@Composable
private fun Group(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tint)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
private fun NameDialog(
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_name)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.take(Habit.MAX_NAME_LENGTH) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
