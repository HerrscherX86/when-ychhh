package id.my.f1zz.whenyah.ui.collections

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.ItemCard
import id.my.f1zz.whenyah.ui.components.ScreenPhase
import id.my.f1zz.whenyah.ui.components.ScrollAwareAddFab
import id.my.f1zz.whenyah.ui.components.rememberFabVisibility
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.AppMotion
import java.util.UUID

@Composable
fun CollectionDetailScreen(
    onItemClick: (UUID) -> Unit,
    onAddItem: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val collection = state.collection
    val listState = rememberLazyListState()
    val fabVisible by rememberFabVisibility(listState)
    val phase = when {
        state.isLoading -> ScreenPhase.Loading
        collection == null -> ScreenPhase.Empty
        else -> ScreenPhase.Content
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = phase,
            animationSpec = tween(
                durationMillis = AppMotion.MediumMillis,
                easing = AppMotion.StandardEasing,
            ),
            label = "collectionDetailPhase",
        ) { targetPhase ->
            when (targetPhase) {
                ScreenPhase.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                ScreenPhase.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = strings.collectionNotFound,
                            message = strings.itemMayBeDeleted,
                        )
                    }
                }

                ScreenPhase.Content -> collection?.let { loadedCollection ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            CollectionHeader(
                                collection = loadedCollection,
                                itemCount = state.items.size,
                                strings = strings,
                            )
                        }

                        if (state.items.isEmpty()) {
                            item {
                                CollectionItemsEmptyState(
                                    strings = strings,
                                    onAddItem = { onAddItem(loadedCollection.id) },
                                )
                            }
                        } else {
                            items(items = state.items, key = { it.item.id }) { entry ->
                                ItemCard(
                                    item = entry.item,
                                    savings = entry.savings,
                                    collectionName = null,
                                    onClick = { onItemClick(entry.item.id) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }

        ScrollAwareAddFab(
            visible = fabVisible && collection != null,
            onClick = { collection?.let { onAddItem(it.id) } },
            contentDescription = strings.addItemDescription,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun CollectionHeader(
    collection: CollectionEntity,
    itemCount: Int,
    strings: AppStrings,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Collections,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(28.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = strings.itemCount(itemCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
private fun CollectionItemsEmptyState(
    strings: AppStrings,
    onAddItem: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(80.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Collections,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Text(
            text = strings.noItemsInCollection,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = strings.noItemsInCollectionMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onAddItem,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.addItem)
        }
    }
}
