package id.my.f1zz.whenyah.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.domain.savings.SavingsSummary
import id.my.f1zz.whenyah.ui.util.formatIdr
import kotlin.math.roundToInt

@Composable
fun ItemCard(
    item: WishlistItemEntity,
    collectionName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    savings: SavingsSummary? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemImage(
                relativePath = item.imageRelativePath,
                contentDescription = item.name,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatIdr(item.targetPriceIdr),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (savings != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { savings.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (savings.isGoalReached) {
                            "Goal reached • ${formatIdr(savings.availableIdr)} saved"
                        } else {
                            "${(savings.progress * 100).roundToInt()}% • " +
                                "${formatIdr(savings.availableIdr)} saved"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (savings.isGoalReached) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val metadata = listOfNotNull(item.category, collectionName)
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")
                if (metadata.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metadata,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
