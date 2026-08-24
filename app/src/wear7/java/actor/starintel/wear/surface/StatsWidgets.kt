package actor.starintel.wear.surface

import android.content.Context
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import actor.starintel.wear.data.StatsCache

class OverviewWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = OverviewWidget()
}

class DocumentsWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = DocumentsWidget()
}

class PeopleWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = PeopleWidget()
}

class TopDTypeWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = TopDTypeWidget()
}

private abstract class CachedStatsWidget : GlanceWearWidget() {
    final override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val text = content(context)
        return WearWidgetDocument(
            background = WearWidgetBrush.color(Color(0xFF101017).rc),
        ) {
            MetricWidgetContent(text)
        }
    }

    abstract fun content(context: Context): String
}

private class OverviewWidget : CachedStatsWidget() {
    override fun content(context: Context): String {
        val stats = StatsCache(context).read()
        return "STARINTEL\nDocs ${short(stats.totalDocuments)} · People ${short(stats.totalPeople)}"
    }
}

private class DocumentsWidget : CachedStatsWidget() {
    override fun content(context: Context): String {
        val stats = StatsCache(context).read()
        return "DOCUMENTS\n${short(stats.totalDocuments)}"
    }
}

private class PeopleWidget : CachedStatsWidget() {
    override fun content(context: Context): String {
        val stats = StatsCache(context).read()
        return "PEOPLE\n${short(stats.totalPeople)}"
    }
}

private class TopDTypeWidget : CachedStatsWidget() {
    override fun content(context: Context): String {
        val top = StatsCache(context).read().byDType.firstOrNull()
        return if (top == null) {
            "DTYPE\nNo data"
        } else {
            "${top.dtype.uppercase()}\n${short(top.count)}"
        }
    }
}

@RemoteComposable
@Composable
private fun MetricWidgetContent(text: String) {
    RemoteBox(
        modifier = RemoteModifier.fillMaxSize(),
        contentAlignment = RemoteAlignment.Center,
    ) {
        RemoteText(
            text = text.rs,
            color = Color(0xFFEAE3FF).rc,
        )
    }
}

private fun short(value: Long): String =
    when {
        value >= 1_000_000_000 -> "%.1fB".format(value / 1_000_000_000.0)
        value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
        value >= 1_000 -> "%.1fK".format(value / 1_000.0)
        else -> value.toString()
    }
