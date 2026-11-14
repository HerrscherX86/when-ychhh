package id.my.f1zz.whenyah.data.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import dagger.hilt.android.EntryPointAccessors
import id.my.f1zz.whenyah.di.WidgetEntryPoint
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.domain.i18n.AppStringsFactory
import id.my.f1zz.whenyah.domain.settings.AppSettings
import kotlinx.coroutines.flow.first

internal data class WidgetData(
    val snapshot: WidgetSnapshot,
    val strings: AppStrings,
)

internal suspend fun loadWidgetData(context: Context): WidgetData {
    val entryPoint = runCatching {
        EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
    }.getOrNull()
    val items = entryPoint?.let { point ->
        runCatching {
            point.wishlistRepository().observeActiveItemsWithSavings().first()
        }.getOrNull()
    }.orEmpty()
    val settings = entryPoint?.let { point ->
        runCatching { point.settingsRepository().currentSettings() }.getOrNull()
    } ?: AppSettings()
    val snapshot = runCatching {
        WidgetSnapshotFactory.create(items)
    }.getOrDefault(WidgetSnapshot())

    return WidgetData(
        snapshot = snapshot,
        strings = AppStringsFactory.forLanguage(settings.language),
    )
}

@Composable
internal fun WidgetTheme(content: @Composable () -> Unit) {
    GlanceTheme(
        colors = ColorProviders(
            light = androidx.compose.material3.lightColorScheme(),
            dark = androidx.compose.material3.darkColorScheme(),
        ),
    ) {
        content()
    }
}

abstract class WhenYahWidgetBase : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    final override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context)
        provideContent {
            WidgetTheme {
                WidgetBody(
                    context = context,
                    snapshot = data.snapshot,
                    strings = data.strings,
                )
            }
        }
    }

    @Composable
    protected abstract fun WidgetBody(
        context: Context,
        snapshot: WidgetSnapshot,
        strings: AppStrings,
    )
}
