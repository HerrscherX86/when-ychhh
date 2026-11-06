package id.my.f1zz.whenyah.ui.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.ui.components.ConfirmDeleteDialog
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.ItemImage
import id.my.f1zz.whenyah.ui.components.SavingsEventRow
import id.my.f1zz.whenyah.ui.util.formatDateTime
import id.my.f1zz.whenyah.ui.util.formatIdr

@Composable
fun ArchivedItemDetailScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchivedItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val restored by viewModel.restored.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val item = state.item

    LaunchedEffect(restored, deleted) {
        if (restored || deleted) onFinished()
    }

    when {
        state.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        item == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "Item not found",
                    message = "It may have been deleted.",
                )
            }
        }

        else -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ItemImage(
                    relativePath = item.imageRelativePath,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = formatIdr(item.targetPriceIdr),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Purchase record",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        val purchase = state.purchaseRecord
                        if (purchase == null) {
                            Text(
                                text = "Purchase record unavailable.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                text = "Purchased ${formatDateTime(purchase.completedAt)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ArchivedDetailRow(
                                label = "Saved before purchase",
                                value = formatIdr(purchase.savingsSpentIdr),
                            )
                            ArchivedDetailRow(
                                label = "Spent from savings",
                                value = formatIdr(purchase.savingsSpentIdr),
                            )
                            Text(
                                text = "Savings spent is the amount you set aside, not necessarily the full purchase price.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                HorizontalDivider()
                Text(
                    text = "Transaction history",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (state.events.isEmpty()) {
                    Text(
                        text = "No ledger entries.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.events.sortedBy { it.createdAt }.forEach { event ->
                        SavingsEventRow(event = event)
                    }
                }

                error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Button(
                    onClick = viewModel::restoreItem,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Restore to active")
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Delete permanently")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete archived item?",
            message = "This permanently deletes the item, image, ledger, plan, and purchase record. It cannot be undone.",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteItem()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ArchivedDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.width(150.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
