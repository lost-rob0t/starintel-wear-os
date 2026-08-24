package actor.starintel.wear.surface

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import actor.starintel.wear.data.StatsCache

class DocumentsComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val value = StatsCache(this).read().totalDocuments
        return metricComplication(request.complicationType, "Docs", value)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        metricComplication(type, "Docs", 128_400)
}

class PeopleComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val value = StatsCache(this).read().totalPeople
        return metricComplication(request.complicationType, "People", value)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        metricComplication(type, "People", 12_800)
}

private fun metricComplication(
    type: ComplicationType,
    label: String,
    value: Long,
): ComplicationData? {
    val short = PlainComplicationText.Builder(short(value)).build()
    val title = PlainComplicationText.Builder(label).build()

    return when (type) {
        ComplicationType.SHORT_TEXT ->
            ShortTextComplicationData.Builder(short, title).build()

        ComplicationType.LONG_TEXT ->
            LongTextComplicationData.Builder(
                PlainComplicationText.Builder("$label ${short(value)}").build(),
                title,
            ).build()

        else -> null
    }
}

private fun short(value: Long): String =
    when {
        value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
        value >= 1_000 -> "%.1fK".format(value / 1_000.0)
        else -> value.toString()
    }
