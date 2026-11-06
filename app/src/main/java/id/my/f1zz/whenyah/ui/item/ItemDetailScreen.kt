package id.my.f1zz.whenyah.ui.item

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.domain.savings.TransactionType
import id.my.f1zz.whenyah.domain.validation.ItemValidation
import id.my.f1zz.whenyah.ui.components.ConfirmDeleteDialog
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.ItemImage
import id.my.f1zz.whenyah.ui.components.SavingsEventRow
import id.my.f1zz.whenyah.ui.components.TransactionDialog
import id.my.f1zz.whenyah.ui.util.formatDateTime
import id.my.f1zz.whenyah.ui.util.formatIdr
import kotlin.math.roundToInt

@Composable
fun ItemDetailScreen(
    onEdit: () -> Unit,
    onViewHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val transactionForm by viewModel.transactionForm.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var urlError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val item = state.item

    LaunchedEffect(deleted) {
        if (deleted) onBack()
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

                SavingsCard(
                    availableIdr = state.savings.availableIdr,
                    targetIdr = state.savings.targetIdr,
                    remainingIdr = state.savings.remainingIdr,
                    surplusIdr = state.savings.surplusIdr,
                    progress = state.savings.progress,
                    isGoalReached = state.savings.isGoalReached,
                    onDeposit = { viewModel.startTransaction(TransactionType.DEPOSIT) },
                    onWithdraw = { viewModel.startTransaction(TransactionType.WITHDRAWAL) },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Delete")
                    }
                }
                HorizontalDivider()
                DetailRow(label = "Collection", value = state.collectionName ?: "None")
                DetailRow(label = "Category", value = item.category ?: "None")
                DetailRow(label = "Added", value = formatDateTime(item.createdAt))
                DetailRow(label = "Updated", value = formatDateTime(item.updatedAt))

                if (!item.notes.isNullOrBlank()) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                val purchaseUrl = item.purchaseUrl
                if (!purchaseUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            if (ItemValidation.isValidPurchaseUrl(purchaseUrl)) {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, purchaseUrl.toUri())
                                    )
                                }.onFailure {
                                    urlError = "No app can open this link."
                                }
                            } else {
                                urlError = "This purchase link is invalid."
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open purchase link")
                    }
                }
                urlError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                HorizontalDivider()
                Text(
                    text = "Recent activity",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (state.events.isEmpty()) {
                    Text(
                        text = "No savings activity yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.events.take(5).forEach { event ->
                        SavingsEventRow(event = event)
                    }
                    TextButton(onClick = onViewHistory) {
                        Text(
                            text = if (state.events.size > 5) {
                                "View all ${state.events.size} transactions"
                            } else {
                                "View all transactions"
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete item?",
            message = "This removes the item, its image, and all related savings data. This cannot be undone.",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteItem()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    transactionForm.type?.let { type ->
        TransactionDialog(
            type = type,
            availableBalanceIdr = state.savings.availableIdr,
            amount = transactionForm.amount,
            note = transactionForm.note,
            isSaving = transactionForm.isSaving,
            error = transactionForm.error,
            onAmountChange = viewModel::onTransactionAmountChange,
            onNoteChange = viewModel::onTransactionNoteChange,
            onDismiss = viewModel::dismissTransaction,
            onSubmit = viewModel::submitTransaction,
        )
    }
}

@Composable
private fun SavingsCard(
    availableIdr: Long,
    targetIdr: Long,
    remainingIdr: Long,
    surplusIdr: Long,
    progress: Float,
    isGoalReached: Boolean,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Savings",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatIdr(availableIdr),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "of ${formatIdr(targetIdr)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isGoalReached) {
                        "Goal reached"
                    } else {
                        "${(progress * 100).roundToInt()}%"
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = if (isGoalReached) {
                        "Target met"
                    } else {
                        "${formatIdr(remainingIdr)} remaining"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (surplusIdr > 0) {
                Text(
                    text = "${formatIdr(surplusIdr)} above target",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onDeposit, modifier = Modifier.weight(1f)) {
                    Text("Add savings")
                }
                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f),
                    enabled = availableIdr > 0,
                ) {
                    Text("Withdraw")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.width(96.dp),
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
