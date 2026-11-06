package id.my.f1zz.whenyah.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.util.formatIdr

@Composable
fun MarkPurchasedDialog(
    itemName: String,
    targetPriceIdr: Long,
    savedIdr: Long,
    isPurchasing: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = {
            if (!isPurchasing) onDismiss()
        },
        title = { Text(strings.markPurchasedTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = strings.targetLabel(formatIdr(targetPriceIdr)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = strings.savedLabel(formatIdr(savedIdr)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (savedIdr > 0) {
                        strings.purchaseSpendInfo(formatIdr(savedIdr))
                    } else {
                        strings.purchaseNoSavingsInfo
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isPurchasing,
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(strings.markPurchasedConfirm)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isPurchasing,
            ) {
                Text(strings.cancel)
            }
        },
    )
}
