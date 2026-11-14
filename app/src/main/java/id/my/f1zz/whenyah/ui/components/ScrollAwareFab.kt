package id.my.f1zz.whenyah.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.my.f1zz.whenyah.ui.theme.AppMotion
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun rememberFabVisibility(listState: LazyListState): State<Boolean> {
    val visible = remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                val scrollingDown = index > previousIndex ||
                    (index == previousIndex && offset > previousOffset)

                if (scrollingDown && (index > 0 || offset > 80)) {
                    visible.value = false
                } else if (!scrollingDown) {
                    visible.value = true
                }

                previousIndex = index
                previousOffset = offset
            }
    }

    return visible
}

@Composable
fun rememberFabVisibility(gridState: LazyGridState): State<Boolean> {
    val visible = remember { mutableStateOf(true) }

    LaunchedEffect(gridState) {
        var previousIndex = gridState.firstVisibleItemIndex
        var previousOffset = gridState.firstVisibleItemScrollOffset

        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                val scrollingDown = index > previousIndex ||
                    (index == previousIndex && offset > previousOffset)

                if (scrollingDown && (index > 0 || offset > 80)) {
                    visible.value = false
                } else if (!scrollingDown) {
                    visible.value = true
                }

                previousIndex = index
                previousOffset = offset
            }
    }

    return visible
}

/**
 * Primary "add" FAB that fades and scales away while the list scrolls down
 * and returns when the user scrolls back toward the top.
 */
@Composable
fun ScrollAwareAddFab(
    visible: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.padding(20.dp),
        enter = fadeIn(
            animationSpec = tween(AppMotion.FastMillis, easing = AppMotion.StandardEasing)
        ) + scaleIn(
            initialScale = 0.85f,
            animationSpec = tween(AppMotion.FastMillis, easing = AppMotion.StandardEasing),
        ),
        exit = fadeOut(
            animationSpec = tween(AppMotion.FastMillis, easing = AppMotion.StandardEasing)
        ) + scaleOut(
            targetScale = 0.85f,
            animationSpec = tween(AppMotion.FastMillis, easing = AppMotion.StandardEasing),
        ),
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = contentDescription,
            )
        }
    }
}
