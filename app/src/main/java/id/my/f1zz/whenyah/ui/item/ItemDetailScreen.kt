package id.my.f1zz.whenyah.ui.item

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.local.entity.SavingsPlanEntity
import id.my.f1zz.whenyah.domain.plan.SavingsPlanProjection
import id.my.f1zz.whenyah.domain.savings.TransactionType
import id.my.f1zz.whenyah.domain.validation.ItemValidation
import id.my.f1zz.whenyah.ui.components.ConfirmDeleteDialog
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.ItemImage
import id.my.f1zz.whenyah.ui.components.LocalAppFeedback
import id.my.f1zz.whenyah.ui.components.MarkPurchasedDialog
import id.my.f1zz.whenyah.ui.components.SavingsEventRow
import id.my.f1zz.whenyah.ui.components.TransactionDialog
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.AppMotion
import id.my.f1zz.whenyah.ui.util.formatDate
import id.my.f1zz.whenyah.ui.util.formatDateTime
import id.my.f1zz.whenyah.ui.util.formatIdr
import kotlin.math.roundToInt

@Composable
fun ItemDetailScreen(
    onEdit: () -> Unit,
    onEditPlan: () -> Unit,
    onViewHistory: () -> Unit,
    onPurchased: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val transactionForm by viewModel.transactionForm.collectAsStateWithLifecycle()
    val purchaseState by viewModel.purchaseState.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    val transactionCompleted by viewModel.transactionCompleted.collectAsStateWithLifecycle()
    val feedback = LocalAppFeedback.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showPurchaseDialog by rememberSaveable { mutableStateOf(false) }
    var urlError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val item = state.item

    LaunchedEffect(deleted) {
        if (deleted) {
            feedback.success(strings.itemDeleted)
            onBack()
        }
    }

    LaunchedEffect(purchaseState.purchased) {
        if (purchaseState.purchased) {
            feedback.success(strings.itemPurchased)
            onPurchased()
        }
    }

    LaunchedEffect(purchaseState.error) {
        purchaseState.error?.let(feedback::error)
    }

    LaunchedEffect(transactionCompleted) {
        when (transactionCompleted) {
            TransactionType.DEPOSIT -> feedback.success(strings.depositRecorded)
            TransactionType.WITHDRAWAL -> feedback.success(strings.withdrawalRecorded)
            null -> Unit
        }
        if (transactionCompleted != null) {
            viewModel.consumeTransactionCompleted()
        }
    }

    LaunchedEffect(transactionForm.error) {
        transactionForm.error?.let(feedback::error)
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
                    title = strings.itemNotFound,
                    message = strings.itemMayBeDeleted,
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

                PlanCard(
                    plan = state.plan,
                    projection = state.planProjection,
                    onEditPlan = onEditPlan,
                )

                Button(
                    onClick = {
                        viewModel.clearPurchaseError()
                        showPurchaseDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(strings.markPurchased)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                        Text(strings.edit)
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(strings.delete)
                    }
                }
                HorizontalDivider()
                DetailRow(
                    label = strings.collectionLabel,
                    value = state.collectionName ?: strings.none,
                )
                DetailRow(
                    label = strings.categoryLabel,
                    value = item.category ?: strings.none,
                )
                DetailRow(
                    label = strings.addedLabel,
                    value = formatDateTime(item.createdAt, strings.locale),
                )
                DetailRow(
                    label = strings.updatedLabel,
                    value = formatDateTime(item.updatedAt, strings.locale),
                )

                if (!item.notes.isNullOrBlank()) {
                    Text(
                        text = strings.notesLabel,
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
                                    urlError = strings.noAppCanOpenLink
                                }
                            } else {
                                urlError = strings.invalidPurchaseLink
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.openPurchaseLink)
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
                    text = strings.recentActivity,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (state.events.isEmpty()) {
                    Text(
                        text = strings.noSavingsActivityYet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.events.take(5).forEach { event ->
                        SavingsEventRow(event = event)
                    }
                    TextButton(onClick = onViewHistory) {
                        Text(strings.viewAllTransactions(state.events.size))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = strings.deleteItemTitle,
            message = strings.deleteItemMessage,
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

    if (showPurchaseDialog && item != null) {
        MarkPurchasedDialog(
            itemName = item.name,
            targetPriceIdr = item.targetPriceIdr,
            savedIdr = state.savings.availableIdr,
            isPurchasing = purchaseState.isPurchasing,
            error = purchaseState.error,
            onConfirm = viewModel::purchaseItem,
            onDismiss = {
                if (!purchaseState.isPurchasing) {
                    showPurchaseDialog = false
                    viewModel.clearPurchaseError()
                }
            },
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
    val strings = LocalAppStrings.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = AppMotion.SlowMillis,
            easing = AppMotion.StandardEasing,
        ),
        label = "itemDetailProgress",
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = strings.savings,
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
                    text = strings.ofTarget(formatIdr(targetIdr)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .semantics {
                        contentDescription = if (isGoalReached) {
                            strings.goalReached
                        } else {
                            strings.percentageFunded(
                                (progress * 100).roundToInt()
                            )
                        }
                    },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isGoalReached) {
                        strings.goalReached
                    } else {
                        strings.percentageFunded((progress * 100).roundToInt())
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = if (isGoalReached) {
                        strings.targetMet
                    } else {
                        strings.remainingLabel(formatIdr(remainingIdr))
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (surplusIdr > 0) {
                Text(
                    text = strings.aboveTarget(formatIdr(surplusIdr)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onDeposit, modifier = Modifier.weight(1f)) {
                    Text(strings.addSavings)
                }
                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f),
                    enabled = availableIdr > 0,
                ) {
                    Text(strings.withdraw)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SavingsPlanEntity?,
    projection: SavingsPlanProjection?,
    onEditPlan: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = strings.savingsPlan,
                style = MaterialTheme.typography.titleMedium,
            )

            if (plan == null) {
                Text(
                    text = strings.noPlanMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onEditPlan) {
                    Text(strings.setAPlan)
                }
            } else {
                Text(
                    text = "${strings.planFrequency(plan.frequency)} • " +
                        formatIdr(plan.contributionIdr),
                    style = MaterialTheme.typography.titleSmall,
                )

                if (!plan.active) {
                    Text(
                        text = strings.planPaused,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                when {
                    projection == null -> {
                        Text(
                            text = strings.noEstimateAvailable,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    projection.isGoalReached -> {
                        Text(
                            text = strings.goalReached,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        projection.completionDate?.let { date ->
                            Text(
                                text = strings.completionDateLabel(
                                    formatDate(date, strings.locale)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    !plan.active -> {
                        Text(
                            text = strings.resumePlanToEstimate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    else -> {
                        Text(
                            text = strings.contributionCount(
                                projection.requiredContributions
                            ),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = strings.remainingValue(
                                formatIdr(projection.remainingIdr)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        val completionDate = projection.completionDate
                        if (completionDate == null) {
                            Text(
                                text = strings.dateTooFar,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            Text(
                                text = strings.estimatedCompletionLabel(
                                    formatDate(completionDate, strings.locale)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = strings.estimateAssumes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                OutlinedButton(onClick = onEditPlan) {
                    Text(strings.editPlan)
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
