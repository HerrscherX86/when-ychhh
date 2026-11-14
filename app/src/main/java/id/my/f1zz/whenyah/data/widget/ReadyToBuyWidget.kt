package id.my.f1zz.whenyah.data.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import id.my.f1zz.whenyah.MainActivity
import id.my.f1zz.whenyah.data.navigation.LaunchContract
import id.my.f1zz.whenyah.domain.format.formatIdr
import id.my.f1zz.whenyah.domain.i18n.AppStrings

class ReadyToBuyWidget : WhenYahWidgetBase() {
    @Composable
    override fun WidgetBody(
        context: Context,
        snapshot: WidgetSnapshot,
        strings: AppStrings,
    ) {
        val colors = GlanceTheme.colors
        val homeIntent = Intent(context, MainActivity::class.java)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.widgetBackground)
                .cornerRadius(24.dp)
                .padding(14.dp)
                .clickable(actionStartActivity(homeIntent)),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = strings.widgetReadyToBuy,
                style = TextStyle(
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            when {
                snapshot.isEmpty -> WidgetEmptyState(context, strings)
                snapshot.readyToBuy.isEmpty() -> {
                    Text(
                        text = strings.widgetNothingReady,
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 13.sp,
                        ),
                        maxLines = 2,
                    )
                }

                else -> {
                    Text(
                        text = strings.widgetReadyCount(snapshot.readyToBuy.size),
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    snapshot.readyToBuy.take(MAX_ROWS).forEach { goal ->
                        val itemIntent = Intent(context, MainActivity::class.java)
                            .putExtra(LaunchContract.EXTRA_ITEM_ID, goal.itemId.toString())
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .clickable(actionStartActivity(itemIntent))
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = goal.name,
                                    style = TextStyle(
                                        color = colors.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    maxLines = 1,
                                )
                                Text(
                                    text = formatIdr(goal.targetIdr),
                                    style = TextStyle(
                                        color = colors.onSurfaceVariant,
                                        fontSize = 11.sp,
                                    ),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    val hidden = snapshot.readyToBuy.size - MAX_ROWS
                    if (hidden > 0) {
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = strings.widgetMore(hidden),
                            style = TextStyle(
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp,
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val MAX_ROWS = 3
    }
}
