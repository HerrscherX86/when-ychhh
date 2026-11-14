package id.my.f1zz.whenyah.data.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import id.my.f1zz.whenyah.MainActivity
import id.my.f1zz.whenyah.data.navigation.LaunchContract
import id.my.f1zz.whenyah.domain.format.formatIdr
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import kotlin.math.roundToInt

class ClosestGoalWidget : WhenYahWidgetBase() {
    @Composable
    override fun WidgetBody(
        context: Context,
        snapshot: WidgetSnapshot,
        strings: AppStrings,
    ) {
        val colors = GlanceTheme.colors
        val goal = snapshot.closestGoal
        val targetIntent = goal?.let {
            Intent(context, MainActivity::class.java)
                .putExtra(LaunchContract.EXTRA_ITEM_ID, it.itemId.toString())
        }
        val homeIntent = targetIntent ?: Intent(context, MainActivity::class.java)

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
                text = strings.widgetClosestGoal,
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
                goal == null -> {
                    Text(
                        text = strings.allActiveGoalsFunded,
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 13.sp,
                        ),
                        maxLines = 2,
                    )
                }

                else -> {
                    Text(
                        text = goal.name,
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 2,
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = formatIdr(goal.remainingIdr),
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = goal.progress,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .semantics {
                                contentDescription = strings.percentageFunded(
                                    (goal.progress * 100).roundToInt()
                                )
                            },
                        color = colors.primary,
                        backgroundColor = colors.surfaceVariant,
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "${strings.percentageFunded((goal.progress * 100).roundToInt())} · " +
                            "${formatIdr(goal.savedIdr)} / ${formatIdr(goal.targetIdr)}",
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
