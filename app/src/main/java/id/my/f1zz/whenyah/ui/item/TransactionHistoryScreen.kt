package id.my.f1zz.whenyah.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.SavingsEventRow
import id.my.f1zz.whenyah.ui.util.formatIdr

@Composable
fun TransactionHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.item

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
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = formatIdr(state.savings.availableIdr),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "of ${formatIdr(item.targetPriceIdr)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LinearProgressIndicator(
                                progress = { state.savings.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "All transactions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                if (state.events.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No transactions yet",
                            message = "Add savings or withdraw from this item to build its history.",
                        )
                    }
                } else {
                    items(items = state.events, key = { it.id }) { event ->
                        SavingsEventRow(event = event)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
