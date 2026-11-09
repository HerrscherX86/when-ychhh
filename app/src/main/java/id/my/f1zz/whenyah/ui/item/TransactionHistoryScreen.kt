package id.my.f1zz.whenyah.ui.item

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.SavingsEventRow
import id.my.f1zz.whenyah.ui.components.ScreenPhase
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.AppMotion
import id.my.f1zz.whenyah.ui.util.formatIdr
import kotlin.math.roundToInt

@Composable
fun TransactionHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val item = state.item
    val animatedProgress by animateFloatAsState(
        targetValue = state.savings.progress,
        animationSpec = tween(
            durationMillis = AppMotion.SlowMillis,
            easing = AppMotion.StandardEasing,
        ),
        label = "historyProgress",
    )
    val phase = when {
        state.isLoading -> ScreenPhase.Loading
        item == null -> ScreenPhase.Empty
        else -> ScreenPhase.Content
    }

    Crossfade(
        targetState = phase,
        animationSpec = tween(
            durationMillis = AppMotion.MediumMillis,
            easing = AppMotion.StandardEasing,
        ),
        label = "historyPhase",
    ) { targetPhase ->
        when (targetPhase) {
            ScreenPhase.Loading -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            ScreenPhase.Empty -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = strings.itemNotFound,
                        message = strings.itemMayBeDeleted,
                    )
                }
            }

            ScreenPhase.Content -> item?.let { loadedItem ->
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
                                    text = loadedItem.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = formatIdr(state.savings.availableIdr),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = strings.ofTarget(formatIdr(loadedItem.targetPriceIdr)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .semantics {
                                            contentDescription = strings.percentageFunded(
                                                (state.savings.progress * 100).roundToInt()
                                            )
                                        },
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = strings.allTransactions,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    if (state.events.isEmpty()) {
                        item {
                            EmptyState(
                                title = strings.noTransactionsYet,
                                message = strings.noTransactionsMessage,
                            )
                        }
                    } else {
                        items(items = state.events, key = { it.id }) { event ->
                            Column(modifier = Modifier.animateItem()) {
                                SavingsEventRow(event = event)
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
