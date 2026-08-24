package actor.starintel.wear.data

import android.content.Context
import actor.starintel.wear.model.DTypeStat
import actor.starintel.wear.model.Stats
import org.json.JSONArray
import org.json.JSONObject

class StatsCache(context: Context) {
    private val prefs = context.getSharedPreferences("starintel.stats", Context.MODE_PRIVATE)

    fun write(stats: Stats) {
        val dtypes = JSONArray()
        stats.byDType.forEach {
            dtypes.put(
                JSONObject()
                    .put("dtype", it.dtype)
                    .put("count", it.count),
            )
        }

        prefs.edit()
            .putLong(KEY_DOCUMENTS, stats.totalDocuments)
            .putLong(KEY_PEOPLE, stats.totalPeople)
            .putString(KEY_DTYPES, dtypes.toString())
            .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    fun read(): Stats {
        val dtypes = runCatching {
            val array = JSONArray(prefs.getString(KEY_DTYPES, "[]"))
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(DTypeStat(item.getString("dtype"), item.getLong("count")))
                }
            }
        }.getOrDefault(emptyList())

        return Stats(
            totalDocuments = prefs.getLong(KEY_DOCUMENTS, 0),
            totalPeople = prefs.getLong(KEY_PEOPLE, 0),
            byDType = dtypes,
        )
    }

    fun updatedAt(): Long = prefs.getLong(KEY_UPDATED_AT, 0)

    private companion object {
        const val KEY_DOCUMENTS = "documents"
        const val KEY_PEOPLE = "people"
        const val KEY_DTYPES = "dtypes"
        const val KEY_UPDATED_AT = "updated_at"
    }
}
