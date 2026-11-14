package id.my.f1zz.whenyah.data.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class WhenYahWidgetType(
    val receiverClass: Class<out GlanceAppWidgetReceiver>,
    val create: () -> GlanceAppWidget,
) {
    OVERVIEW(WhenYahWidgetReceiver::class.java, ::WhenYahWidget),
    CLOSEST_GOAL(ClosestGoalWidgetReceiver::class.java, ::ClosestGoalWidget),
    READY_TO_BUY(ReadyToBuyWidgetReceiver::class.java, ::ReadyToBuyWidget),
}

@Singleton
class WidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun refresh() {
        WhenYahWidgetType.entries.forEach { type ->
            runCatching { type.create().updateAll(context) }
        }
    }

    fun isPinSupported(): Boolean =
        context.getSystemService(AppWidgetManager::class.java)
            ?.isRequestPinAppWidgetSupported == true

    @Suppress("UNCHECKED_CAST")
    suspend fun requestPin(type: WhenYahWidgetType): Boolean = runCatching {
        GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
            type.receiverClass as Class<GlanceAppWidgetReceiver>,
            type.create(),
        )
    }.getOrDefault(false)
}
