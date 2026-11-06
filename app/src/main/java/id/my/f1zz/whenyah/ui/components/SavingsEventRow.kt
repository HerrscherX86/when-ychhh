package id.my.f1zz.whenyah.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.my.f1zz.whenyah.data.local.entity.SavingsEventEntity
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.ui.util.formatDateTime
import id.my.f1zz.whenyah.ui.util.formatIdr

@Composable
fun SavingsEventRow(
    event: SavingsEventEntity,
    modifier: Modifier = Modifier,
) {
    val isDeposit = event.type == SavingsEventType.DEPOSIT
    val label = when (event.type) {
        SavingsEventType.DEPOSIT -> "Deposit"
        SavingsEventType.WITHDRAWAL -> "Withdrawal"
        SavingsEventType.PURCHASE_SPEND -> "Spent on purchase"
    }
    val amountColor: Color = when (event.type) {
        SavingsEventType.DEPOSIT -> MaterialTheme.colorScheme.primary
        SavingsEventType.WITHDRAWAL -> MaterialTheme.colorScheme.error
        SavingsEventType.PURCHASE_SPEND -> MaterialTheme.colorScheme.secondary
    }
    val icon = when (event.type) {
        SavingsEventType.DEPOSIT -> Icons.Outlined.Add
        SavingsEventType.WITHDRAWAL -> Icons.Outlined.Remove
        SavingsEventType.PURCHASE_SPEND -> Icons.Outlined.ShoppingCart
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = amountColor,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
            )
            if (!event.note.isNullOrBlank()) {
                Text(
                    text = event.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatDateTime(event.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${if (isDeposit) "+" else "-"}${formatIdr(event.amountIdr)}",
            style = MaterialTheme.typography.titleSmall,
            color = amountColor,
        )
    }
}
