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
    AlertDialog(
        onDismissRequest = {
            if (!isPurchasing) onDismiss()
        },
        title = { Text("Mark purchased?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "Target: ${formatIdr(targetPriceIdr)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Saved: ${formatIdr(savedIdr)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (savedIdr > 0) {
                        "This records a purchase spend of ${formatIdr(savedIdr)} and moves the " +
                            "item to the archive. It does not process a real payment."
                    } else {
                        "No savings are recorded, so the item will be archived without a spend " +
                            "event. It does not process a real payment."
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
                    Text("Mark purchased")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isPurchasing,
            ) {
                Text("Cancel")
            }
        },
    )
}
