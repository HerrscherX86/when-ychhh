package id.my.f1zz.whenyah.ui.item

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.data.local.entity.CollectionEntity
import id.my.f1zz.whenyah.ui.components.ItemImage
import java.util.UUID

@Composable
fun ItemEditorScreen(
    onSaved: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    LaunchedEffect(state.savedItemId) {
        state.savedItemId?.let(onSaved)
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
        ItemImage(
            relativePath = state.imageRelativePath,
            contentDescription = "Selected item image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            ) {
                Text(if (state.imageRelativePath == null) "Choose image" else "Change image")
            }
            if (state.imageRelativePath != null) {
                TextButton(onClick = viewModel::onRemoveImage) {
                    Text("Remove")
                }
            }
        }

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true,
            isError = state.nameError != null,
            supportingText = state.nameError?.let { message -> { Text(message) } },
        )

        OutlinedTextField(
            value = state.price,
            onValueChange = viewModel::onPriceChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Target price (IDR)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.priceError != null,
            supportingText = state.priceError?.let { message -> { Text(message) } },
        )

        OutlinedTextField(
            value = state.category,
            onValueChange = viewModel::onCategoryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Category (optional)") },
            singleLine = true,
        )

        Text(
            text = "Collection",
            style = MaterialTheme.typography.labelLarge,
        )
        CollectionSelector(
            collections = state.collections,
            selectedId = state.collectionId,
            onSelect = viewModel::onCollectionChange,
        )

        OutlinedTextField(
            value = state.purchaseUrl,
            onValueChange = viewModel::onPurchaseUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Purchase URL (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            isError = state.purchaseUrlError != null,
            supportingText = state.purchaseUrlError?.let { message -> { Text(message) } },
        )

        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") },
            minLines = 3,
            maxLines = 6,
        )

        state.saveError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = viewModel::save,
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
                Text(if (state.isEditing) "Save changes" else "Add item")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CollectionSelector(
    collections: List<CollectionEntity>,
    selectedId: UUID?,
    onSelect: (UUID?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = collections.firstOrNull { it.id == selectedId }?.name ?: "No collection"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = selectedName,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
            )
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("No collection") },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            collections.forEach { collection ->
                DropdownMenuItem(
                    text = { Text(collection.name) },
                    onClick = {
                        onSelect(collection.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
