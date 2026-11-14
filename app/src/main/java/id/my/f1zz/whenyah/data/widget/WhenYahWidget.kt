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
import id.my.f1zz.whenyah.domain.format.formatIdr
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import kotlin.math.roundToInt

class WhenYahWidget : WhenYahWidgetBase() {
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
                text = strings.appName,
                style = TextStyle(
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = formatIdr(snapshot.totalSavedIdr),
                style = TextStyle(
                    color = colors.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            LinearProgressIndicator(
                progress = snapshot.overallProgress,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .semantics {
                        contentDescription = strings.percentageFunded(
                            (snapshot.overallProgress * 100).roundToInt()
                        )
                    },
                color = if (snapshot.overallProgress >= 1f) {
                    colors.tertiary
                } else {
                    colors.primary
                },
                backgroundColor = colors.surfaceVariant,
            )
            Spacer(modifier = GlanceModifier.height(8.dp))

            if (snapshot.isEmpty) {
                WidgetEmptyState(context, strings)
            } else {
                Text(
                    text = strings.widgetSummary(
                        active = snapshot.activeItemCount,
                        funded = snapshot.fullyFundedCount,
                    ),
                    style = TextStyle(
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}
