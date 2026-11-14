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
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import id.my.f1zz.whenyah.MainActivity
import id.my.f1zz.whenyah.data.navigation.LaunchContract
import id.my.f1zz.whenyah.domain.i18n.AppStrings

@Composable
internal fun WidgetEmptyState(context: Context, strings: AppStrings) {
    val colors = GlanceTheme.colors
    Text(
        text = strings.noItemsYet,
        style = TextStyle(
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
        ),
        maxLines = 1,
    )
    Spacer(modifier = GlanceModifier.height(3.dp))
    Text(
        text = strings.addItem,
        modifier = GlanceModifier.clickable(
            actionStartActivity(
                Intent(context, MainActivity::class.java)
                    .putExtra(LaunchContract.EXTRA_OPEN_ADD_ITEM, true)
            )
        ),
        style = TextStyle(
            color = colors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
        maxLines = 1,
    )
}
