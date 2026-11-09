package id.my.f1zz.whenyah.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.my.f1zz.whenyah.data.update.UpdateManifest
import id.my.f1zz.whenyah.domain.i18n.AppStrings

@Composable
fun UpdateAvailableDialog(
    manifest: UpdateManifest,
    strings: AppStrings,
    onUpdate: () -> Unit,
    onLater: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onLater,
        modifier = modifier,
        title = { Text(strings.updateAvailableTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.updateAvailableMessage(manifest.versionName))
                manifest.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = onSkip,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = strings.skipVersion,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(strings.updateNow)
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text(strings.later)
            }
        },
    )
}
