package id.my.f1zz.whenyah.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.plan.SavingsPlanProjection
import id.my.f1zz.whenyah.ui.components.ConfirmDeleteDialog
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.util.formatDate
import id.my.f1zz.whenyah.ui.util.formatIdr
import id.my.f1zz.whenyah.ui.util.formatPlanFrequency

@Composable
fun SavingsPlanScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavingsPlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onFinished()
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val item = state.item
    if (item == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = "Item not found",
                message = "It may have been deleted.",
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Target ${formatIdr(item.targetPriceIdr)} • Saved ${formatIdr(state.availableIdr)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.contribution,
            onValueChange = viewModel::onContributionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contribution amount (IDR)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.error != null,
        )

        Text(
            text = "Frequency",
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlanFrequency.entries.forEach { frequency ->
                FilterChip(
                    selected = state.frequency == frequency,
                    onClick = { viewModel.onFrequencyChange(frequency) },
                    label = { Text(formatPlanFrequency(frequency)) },
                )
            }
        }

        ProjectionCard(projection = state.projection)

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = viewModel::savePlan,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(if (state.plan == null) "Create plan" else "Update plan")
            }
        }

        state.plan?.let { plan ->
            HorizontalDivider()
            Text(
                text = "Plan status",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (plan.active) "Active" else "Paused",
                style = MaterialTheme.typography.bodyMedium,
                color = if (plan.active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            OutlinedButton(
                onClick = viewModel::togglePlanActive,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (plan.active) "Pause plan" else "Resume plan")
            }
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete plan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete savings plan?",
            message = "The saved contributions stay in the ledger. Only the plan and its estimate are removed.",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deletePlan()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ProjectionCard(projection: SavingsPlanProjection?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Projection",
                style = MaterialTheme.typography.titleMedium,
            )

            when {
                projection == null -> {
                    Text(
                        text = "Enter a contribution amount to see an estimate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                projection.isGoalReached -> {
                    Text(
                        text = "Goal reached",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    projection.completionDate?.let { date ->
                        Text(
                            text = "Completion date: ${formatDate(date)}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                else -> {
                    val contributionLabel = if (projection.requiredContributions == 1L) {
                        "1 planned contribution"
                    } else {
                        "${projection.requiredContributions} planned contributions"
                    }
                    Text(
                        text = contributionLabel,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "Remaining: ${formatIdr(projection.remainingIdr)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    val completionDate = projection.completionDate
                    if (completionDate == null) {
                        Text(
                            text = "The projected date is too far in the future.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(
                            text = "Estimated completion: ${formatDate(completionDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = "Estimate assumes contributions beginning today and continuing on schedule.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
