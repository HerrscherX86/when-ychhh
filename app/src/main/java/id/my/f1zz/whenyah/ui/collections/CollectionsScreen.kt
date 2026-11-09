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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.ui.components.CollectionNameDialog
import id.my.f1zz.whenyah.ui.components.ConfirmDeleteDialog
import id.my.f1zz.whenyah.ui.components.LocalAppFeedback
import id.my.f1zz.whenyah.ui.components.ScreenPhase
import id.my.f1zz.whenyah.ui.components.ScrollAwareAddFab
import id.my.f1zz.whenyah.ui.components.rememberFabVisibility
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.AppMotion
import java.util.UUID

@Composable
fun CollectionsScreen(
    onCollectionClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val feedback = LocalAppFeedback.current
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<CollectionEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CollectionEntity?>(null) }
    val totalItems = state.collections.sumOf { it.itemCount }
    val listState = rememberLazyListState()
    val fabVisible by rememberFabVisibility(listState)
    val phase = when {
        state.isLoading -> ScreenPhase.Loading
        state.collections.isEmpty() -> ScreenPhase.Empty
        else -> ScreenPhase.Content
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = phase,
            animationSpec = tween(
                durationMillis = AppMotion.MediumMillis,
                easing = AppMotion.StandardEasing,
            ),
            label = "collectionsPhase",
        ) { targetPhase ->
            when (targetPhase) {
                ScreenPhase.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                ScreenPhase.Empty -> {
                    CollectionsEmptyState(
                        strings = strings,
                        onCreate = { showCreateDialog = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                ScreenPhase.Content -> {
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
                            CollectionsOverviewCard(
                                collectionCount = state.collections.size,
                                itemCount = totalItems,
                                strings = strings,
                            )
                        }
                        items(items = state.collections, key = { it.collection.id }) { summary ->
                            CollectionCard(
                                collection = summary.collection,
                                itemCount = summary.itemCount,
                                strings = strings,
                                onClick = { onCollectionClick(summary.collection.id) },
                                onRename = { renameTarget = summary.collection },
                                onDelete = { deleteTarget = summary.collection },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }

        ScrollAwareAddFab(
            visible = fabVisible,
            onClick = { showCreateDialog = true },
            contentDescription = strings.addCollectionDescription,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }

    if (showCreateDialog) {
        CollectionNameDialog(
            title = strings.newCollection,
            initialName = "",
            confirmLabel = strings.create,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createCollection(name)
                feedback.success(strings.collectionCreated)
                showCreateDialog = false
            },
        )
    }

    renameTarget?.let { collection ->
        CollectionNameDialog(
            title = strings.renameCollection,
            initialName = collection.name,
            confirmLabel = strings.save,
            onDismiss = { renameTarget = null },
            onConfirm = { name ->
                viewModel.renameCollection(collection.id, name)
                feedback.success(strings.collectionRenamed)
                renameTarget = null
            },
        )
    }

    deleteTarget?.let { collection ->
        ConfirmDeleteDialog(
            title = strings.deleteCollectionTitle,
            message = strings.deleteCollectionMessage(collection.name),
            onConfirm = {
                viewModel.deleteCollection(collection.id)
                feedback.success(strings.collectionDeleted)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun CollectionsOverviewCard(
    collectionCount: Int,
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
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(
                    text = strings.collectionsOverviewTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                CollectionMetric(
                    value = collectionCount.toString(),
                    label = strings.navCollections,
                    modifier = Modifier.weight(1f),
                )
                CollectionMetric(
                    value = itemCount.toString(),
                    label = strings.organizedItemsLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CollectionMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun CollectionCard(
    collection: CollectionEntity,
    itemCount: Int,
    strings: AppStrings,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = strings.itemCount(itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CollectionActionsMenu(
                strings = strings,
                onRename = onRename,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun CollectionActionsMenu(
    strings: AppStrings,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = strings.moreActions,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(strings.renameCollection) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
                },
                onClick = {
                    expanded = false
                    onRename()
                },
            )
            DropdownMenuItem(
                text = { Text(strings.deleteCollection) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun CollectionsEmptyState(
    strings: AppStrings,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(96.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                    )
                }
            }
            Text(
                text = strings.noCollectionsYet,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = strings.noCollectionsMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onCreate,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.createCollectionAction)
            }
        }
    }
}
