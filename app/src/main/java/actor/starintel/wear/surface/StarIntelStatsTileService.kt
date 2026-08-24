package actor.starintel.wear.surface

import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import actor.starintel.wear.data.StatsCache
import com.google.common.util.concurrent.Futures

class StarIntelStatsTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTileTimeline(
                    Timeline.fromLayoutElement(
                        materialScope(this, requestParams.deviceConfiguration) {
                            val stats = StatsCache(this@StarIntelStatsTileService).read()
                            primaryLayout(
                                titleSlot = {
                                    text(
                                        "StarIntel".layoutString,
                                        typography = TITLE_MEDIUM,
                                    )
                                },
                                mainSlot = {
                                    text(
                                        (
                                            "Docs ${format(stats.totalDocuments)}\n" +
                                                "People ${format(stats.totalPeople)}"
                                            ).layoutString,
                                        typography = BODY_LARGE,
                                    )
                                },
                            )
                        },
                    ),
                )
                .build(),
        )

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest) =
        Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build(),
        )

    private fun format(value: Long): String =
        when {
            value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
            value >= 1_000 -> "%.1fK".format(value / 1_000.0)
            else -> value.toString()
        }

    private companion object {
        const val RESOURCES_VERSION = "1"
    }
}
