package io.github.codingfulalt.cairn.core.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.codingfulalt.cairn.R

class NotificationPermission(
    val denied: Boolean,
    val request: () -> Unit,
    val openSettings: () -> Unit,
)

@Composable
fun rememberNotificationPermission(): NotificationPermission {
    val context = LocalContext.current
    var denied by remember { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            denied = !granted
        }
    return NotificationPermission(
        denied = denied,
        request = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // nothing to ask for below 13, they can only be switched off in settings
                denied = !NotificationManagerCompat.from(context).areNotificationsEnabled()
            } else {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                val granted = PackageManager.PERMISSION_GRANTED
                if (ContextCompat.checkSelfPermission(context, permission) == granted) {
                    denied = false
                } else {
                    launcher.launch(permission)
                }
            }
        },
        openSettings = {
            context.startActivity(
                Intent(
                    Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                ).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
            )
        },
    )
}

@Composable
fun NotificationsBlockedBanner(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.NotificationsOff, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.notifications_blocked),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            )
            TextButton(
                onClick = onOpenSettings,
            ) { Text(stringResource(R.string.action_open_settings)) }
        }
    }
}
