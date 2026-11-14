package id.my.f1zz.whenyah.ui.item

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.ui.components.ItemImage
import id.my.f1zz.whenyah.ui.components.AdaptiveContent
import id.my.f1zz.whenyah.ui.components.LocalAppFeedback
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.navigation.LocalSetTopBarBackHandler
import id.my.f1zz.whenyah.ui.util.IdrAmountVisualTransformation
import id.my.f1zz.whenyah.ui.util.formatIdrDigits
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorScreen(
    onSaved: (UUID) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val feedback = LocalAppFeedback.current
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }
    val setTopBarBackHandler = LocalSetTopBarBackHandler.current
    val latestState by rememberUpdatedState(state)
    val latestOnBack by rememberUpdatedState(onBack)

    var showMoreDetails by rememberSaveable { mutableStateOf(false) }
    var showCollectionSheet by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val collectionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    LaunchedEffect(state.savedItemId) {
        state.savedItemId?.let { savedId ->
            feedback.success(if (state.isEditing) strings.itemUpdated else strings.itemCreated)
            onSaved(savedId)
        }
    }

    LaunchedEffect(state.saveError) {
        state.saveError?.let(feedback::error)
    }

    LaunchedEffect(state.nameError, state.priceError) {
        when {
            state.nameError != null -> nameFocusRequester.requestFocus()
            state.priceError != null -> priceFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(state.isEditing, state.isLoading) {
        if (state.isEditing &&
            !state.isLoading &&
            (state.category.isNotBlank() || state.notes.isNotBlank() || state.purchaseUrl.isNotBlank())
        ) {
            showMoreDetails = true
        }
    }

    LaunchedEffect(state.purchaseUrlError) {
        if (state.purchaseUrlError != null) showMoreDetails = true
    }

    LaunchedEffect(Unit) {
        setTopBarBackHandler {
            if (!latestState.isSaving && !latestState.isImageLoading) {
                if (latestState.isDirty) {
                    showDiscardDialog = true
                } else {
                    latestOnBack()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { setTopBarBackHandler(null) }
    }

    BackHandler(enabled = state.isDirty || state.isSaving || state.isImageLoading) {
        if (!state.isSaving && !state.isImageLoading) {
            if (state.isDirty) {
                showDiscardDialog = true
            } else {
                onBack()
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        AdaptiveContent(modifier = modifier, maxWidth = 720.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                EditorSection(title = strings.sectionItemInfo) {
                    ImageUploadArea(
                        imageRelativePath = state.imageRelativePath,
                        isLoading = state.isImageLoading,
                        strings = strings,
                        onPick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        onRemove = viewModel::onRemoveImage,
                    )

                    TextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester),
                        label = { Text(strings.nameLabel) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge,
                        shape = RoundedCornerShape(20.dp),
                        colors = editorTextFieldColors(),
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { message ->
                            {
                                Text(
                                    text = message,
                                    modifier = Modifier.semantics {
                                        liveRegion = LiveRegionMode.Assertive
                                    },
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )

                    TextField(
                        value = state.price,
                        onValueChange = viewModel::onPriceChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(priceFocusRequester),
                        label = { Text(strings.targetPriceIdr) },
                        prefix = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rp",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        visualTransformation = IdrAmountVisualTransformation(),
                        shape = RoundedCornerShape(20.dp),
                        colors = editorTextFieldColors(),
                        isError = state.priceError != null,
                        supportingText = state.priceError?.let { message ->
                            {
                                Text(
                                    text = message,
                                    modifier = Modifier.semantics {
                                        liveRegion = LiveRegionMode.Assertive
                                    },
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                    )
                }

                EditorSection(title = strings.sectionOrganization) {
                    CollectionSelector(
                        collections = state.collections,
                        selectedId = state.collectionId,
                        strings = strings,
                        onClick = { showCollectionSheet = true },
                    )
                    state.collectionError?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                AdditionalDetailsSection(
                    expanded = showMoreDetails,
                    strings = strings,
                    onToggle = { showMoreDetails = !showMoreDetails },
                    category = state.category,
                    onCategoryChange = viewModel::onCategoryChange,
                    categorySuggestions = state.categorySuggestions,
                    purchaseUrl = state.purchaseUrl,
                    onPurchaseUrlChange = viewModel::onPurchaseUrlChange,
                    notes = state.notes,
                    onNotesChange = viewModel::onNotesChange,
                    purchaseUrlError = state.purchaseUrlError,
                    focusManager = focusManager,
                )
            }

            SaveBar(
                isEditing = state.isEditing,
                isSaving = state.isSaving,
                isImageLoading = state.isImageLoading,
                saveError = state.saveError,
                strings = strings,
                onSave = {
                    focusManager.clearFocus()
                    viewModel.save()
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        }
    }

    if (showCollectionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCollectionSheet = false },
            sheetState = collectionSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            CollectionSheetContent(
                collections = state.collections,
                selectedId = state.collectionId,
                strings = strings,
                onSelect = viewModel::onCollectionChange,
                onCreate = viewModel::createCollection,
                onDismiss = { showCollectionSheet = false },
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(strings.unsavedChangesTitle) },
            text = { Text(strings.unsavedChangesMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(strings.discardChanges)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(strings.keepEditing)
                }
            },
        )
    }
}

@Composable
private fun EditorSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}

@Composable
private fun ImageUploadArea(
    imageRelativePath: String?,
    isLoading: Boolean,
    strings: AppStrings,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    val hasImage = imageRelativePath != null
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val shape = RoundedCornerShape(24.dp)

    if (hasImage) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) {
                ItemImage(
                    relativePath = imageRelativePath,
                    contentDescription = strings.selectedItemImage,
                    modifier = Modifier.fillMaxSize(),
                    shape = shape,
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 2.dp,
                ) {
                    Row {
                        IconButton(
                            onClick = onPick,
                            enabled = !isLoading,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = strings.changeImage,
                            )
                        }
                        IconButton(
                            onClick = onRemove,
                            enabled = !isLoading,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = strings.removeImage,
                            )
                        }
                    }
                }
                if (isLoading) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    } else {
        Card(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            enabled = !isLoading,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = strings.itemPhoto,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = strings.tapToChooseImage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CollectionSelector(
    collections: List<CollectionEntity>,
    selectedId: UUID?,
    strings: AppStrings,
    onClick: () -> Unit,
) {
    val selectedName = collections.firstOrNull { it.id == selectedId }?.name
        ?: strings.noCollection

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Collections,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.collectionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = selectedName,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdditionalDetailsSection(
    expanded: Boolean,
    strings: AppStrings,
    onToggle: () -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    categorySuggestions: List<String>,
    purchaseUrl: String,
    onPurchaseUrlChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    purchaseUrlError: String?,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.sectionAdditionalDetails,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) strings.hideDetails else strings.showDetails,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (categorySuggestions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = strings.categorySuggestionsLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                categorySuggestions.forEach { suggestion ->
                                    val isSelected =
                                        category.equals(suggestion, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onCategoryChange(if (isSelected) "" else suggestion)
                                        },
                                        label = { Text(suggestion) },
                                    )
                                }
                            }
                        }
                    }
                    TextField(
                        value = category,
                        onValueChange = onCategoryChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.categoryOptional) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(20.dp),
                        colors = editorTextFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )

                    TextField(
                        value = purchaseUrl,
                        onValueChange = onPurchaseUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.purchaseUrlOptional) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(20.dp),
                        colors = editorTextFieldColors(),
                        isError = purchaseUrlError != null,
                        supportingText = purchaseUrlError?.let { message -> { Text(message) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )

                    TextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.notesOptional) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(20.dp),
                        colors = editorTextFieldColors(),
                        minLines = 3,
                        maxLines = 6,
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveBar(
    isEditing: Boolean,
    isSaving: Boolean,
    isImageLoading: Boolean,
    saveError: String?,
    strings: AppStrings,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                saveError?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = !isSaving && !isImageLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.saving)
                    } else {
                        Text(if (isEditing) strings.saveChanges else strings.addItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionSheetContent(
    collections: List<CollectionEntity>,
    selectedId: UUID?,
    strings: AppStrings,
    onSelect: (UUID?) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showCreateField by rememberSaveable { mutableStateOf(false) }
    var collectionName by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.chooseCollection,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = strings.cancel,
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
        ) {
            item {
                CollectionOptionRow(
                    name = strings.noCollection,
                    selected = selectedId == null,
                    onClick = {
                        onSelect(null)
                        onDismiss()
                    },
                )
            }
            items(items = collections, key = { it.id }) { collection ->
                CollectionOptionRow(
                    name = collection.name,
                    selected = selectedId == collection.id,
                    onClick = {
                        onSelect(collection.id)
                        onDismiss()
                    },
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        if (showCreateField) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(strings.collectionNameLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = editorTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (collectionName.isNotBlank()) {
                                onCreate(collectionName)
                                collectionName = ""
                                showCreateField = false
                                onDismiss()
                            }
                        }
                    ),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showCreateField = false
                            collectionName = ""
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(strings.cancel)
                    }
                    Button(
                        onClick = {
                            if (collectionName.isNotBlank()) {
                                onCreate(collectionName)
                                collectionName = ""
                                showCreateField = false
                                onDismiss()
                            }
                        },
                        enabled = collectionName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(strings.create)
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = { showCreateField = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .heightIn(min = 52.dp),
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

@Composable
private fun CollectionOptionRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .heightIn(min = 56.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun editorTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    errorContainerColor = MaterialTheme.colorScheme.errorContainer,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
)

