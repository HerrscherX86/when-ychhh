package id.my.f1zz.whenyah.ui.item

import android.app.TimePickerDialog
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.plan.SavingsPlanProjection
import id.my.f1zz.whenyah.ui.components.ConfirmDeleteDialog
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.util.formatDate
import id.my.f1zz.whenyah.ui.util.formatIdr
import id.my.f1zz.whenyah.ui.util.formatTime
import java.time.LocalTime

@Composable
fun SavingsPlanScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavingsPlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val context = LocalContext.current
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
                title = strings.itemNotFound,
                message = strings.itemMayBeDeleted,
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
            text = strings.targetAndSaved(
                formatIdr(item.targetPriceIdr),
                formatIdr(state.availableIdr),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.contribution,
            onValueChange = viewModel::onContributionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(strings.contributionAmountIdr) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.error != null,
        )

        Text(
            text = strings.frequency,
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlanFrequency.entries.forEach { frequency ->
                FilterChip(
                    selected = state.frequency == frequency,
                    onClick = { viewModel.onFrequencyChange(frequency) },
                    label = { Text(strings.planFrequency(frequency)) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.contributionReminder,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = strings.reminderHelp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = state.reminderEnabled,
                onCheckedChange = viewModel::onReminderEnabledChange,
            )
        }

        if (state.reminderEnabled) {
            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            viewModel.onReminderTimeChange(LocalTime.of(hour, minute))
                        },
                        state.reminderLocalTime.hour,
                        state.reminderLocalTime.minute,
                        true,
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.reminderTimeLabel(formatTime(state.reminderLocalTime)))
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
                Text(if (state.plan == null) strings.createPlan else strings.updatePlan)
            }
        }

        state.plan?.let { plan ->
            HorizontalDivider()
            Text(
                text = strings.planStatus,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (plan.active) strings.active else strings.paused,
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
                Text(if (plan.active) strings.pausePlan else strings.resumePlan)
            }
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(strings.deletePlan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = strings.deletePlanTitle,
            message = strings.deletePlanMessage,
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
    val strings = LocalAppStrings.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = strings.projection,
                style = MaterialTheme.typography.titleMedium,
            )

            when {
                projection == null -> {
                    Text(
                        text = strings.enterContributionEstimate,
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

                else -> {
                    Text(
                        text = strings.contributionCount(projection.requiredContributions),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = strings.remainingValue(formatIdr(projection.remainingIdr)),
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
        }
    }
}
