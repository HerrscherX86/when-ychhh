package id.my.f1zz.whenyah.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.f1zz.whenyah.ui.components.AdaptiveContent
import id.my.f1zz.whenyah.ui.components.EmptyState
import id.my.f1zz.whenyah.ui.components.SavingsEventRow
import id.my.f1zz.whenyah.ui.components.ScreenPhase
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import id.my.f1zz.whenyah.ui.theme.AppMotion

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val phase = when {
        state.isLoading -> ScreenPhase.Loading
        state.activities.isEmpty() -> ScreenPhase.Empty
        else -> ScreenPhase.Content
    }

    AdaptiveContent(modifier = modifier, maxWidth = 720.dp) {
        Crossfade(
            targetState = phase,
            animationSpec = tween(
                durationMillis = AppMotion.MediumMillis,
                easing = AppMotion.StandardEasing,
            ),
            label = "activityPhase",
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
                        title = strings.noActivityYet,
                        message = strings.noActivityMessage,
                    )
                }
            }

            ScreenPhase.Content -> {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    item {
                        Text(
                            text = strings.transactionCount(state.activities.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    itemsIndexed(
                        items = state.activities,
                        key = { _, activity -> "activity-${activity.event.id}" },
                    ) { index, activity ->
                        Column(modifier = Modifier.animateItem()) {
                            SavingsEventRow(
                                event = activity.event,
                                itemName = activity.itemName,
                            )
                            if (index < state.activities.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
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
