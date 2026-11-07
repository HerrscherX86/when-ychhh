package id.my.f1zz.whenyah.ui.archive

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.ItemImage
import id.my.f1zz.whenyah.ui.components.ScreenPhase
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.AppMotion
import id.my.f1zz.whenyah.ui.util.formatDateTime
import id.my.f1zz.whenyah.ui.util.formatIdr
import java.util.UUID

@Composable
fun ArchiveScreen(
    onItemClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val phase = when {
        state.isLoading -> ScreenPhase.Loading
        state.items.isEmpty() -> ScreenPhase.Empty
        else -> ScreenPhase.Content
    }

    Crossfade(
        targetState = phase,
        animationSpec = tween(
            durationMillis = AppMotion.MediumMillis,
            easing = AppMotion.StandardEasing,
        ),
        label = "archivePhase",
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
                        title = strings.noPurchasedItemsYet,
                        message = strings.noPurchasedItemsMessage,
                    )
                }
            }

            ScreenPhase.Content -> {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = state.items, key = { it.item.id }) { entry ->
                        Card(
                            onClick = { onItemClick(entry.item.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ItemImage(
                                    relativePath = entry.item.imageRelativePath,
                                    contentDescription = entry.item.name,
                                    modifier = Modifier.size(72.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = formatIdr(entry.item.targetPriceIdr),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    val purchase = entry.purchaseRecord
                                    if (purchase != null) {
                                        Text(
                                            text = strings.purchasedOn(
                                                formatDateTime(
                                                    purchase.completedAt,
                                                    strings.locale,
                                                )
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = strings.spentFromSavings(
                                                formatIdr(purchase.savingsSpentIdr)
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    } else {
                                        Text(
                                            text = strings.purchaseDetailsUnavailable,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
