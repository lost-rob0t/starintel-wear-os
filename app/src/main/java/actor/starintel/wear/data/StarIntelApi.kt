package actor.starintel.wear.data

import actor.starintel.wear.model.AuthMode
import actor.starintel.wear.model.CreatedTarget
import actor.starintel.wear.model.DTypeStat
import actor.starintel.wear.model.SearchResult
import actor.starintel.wear.model.Settings
import actor.starintel.wear.model.Stats
import actor.starintel.wear.model.TargetDraft
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

class StarIntelApi(
    private val settings: Settings,
    private val token: String?,
) {
    fun fetchStats(): Stats {
        val body = request("GET", "/api/v1/stats")
        val root = JSONObject(body)

        val docs = root.optLong("total_documents", root.optLong("documents", 0))
        val people = root.optLong("total_people", root.optLong("people", 0))
        val dtypesObject =
            root.optJSONObject("by_dtype")
                ?: root.optJSONObject("dtypes")
                ?: JSONObject()

        val dtypes = buildList {
            for (key in dtypesObject.keys()) {
                add(DTypeStat(key, dtypesObject.optLong(key, 0)))
            }
        }.sortedByDescending { it.count }

        return Stats(docs, people, dtypes)
    }

    fun search(query: String, limit: Int = 10): List<SearchResult> {
        val q = URLEncoder.encode(query, Charsets.UTF_8.name())
        val body = request("GET", "/api/v1/search?q=$q&limit=$limit")

        val array = when {
            body.trimStart().startsWith("[") -> JSONArray(body)
            else -> JSONObject(body).optJSONArray("results") ?: JSONArray()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    SearchResult(
                        id = item.optString("id", item.optString("_id", "")),
                        title = item.optString(
                            "title",
                            item.optString("name", "Untitled"),
                        ),
                        dtype = item.optString("dtype", "document"),
                        summary = item.optString("summary").takeIf { it.isNotBlank() },
                        url = item.optString("url").takeIf { it.isNotBlank() },
                    ),
                )
            }
        }
    }

    fun createTarget(draft: TargetDraft): CreatedTarget {
        require(settings.authMode != AuthMode.NONE) {
            "Authentication is required to create targets."
        }

        val body = JSONObject()
            .put("name", draft.name)
            .put("dtype", draft.dtype)
            .toString()

        val response = JSONObject(request("POST", "/api/v1/targets", body))
        return CreatedTarget(
            id = response.optString("id", response.optString("_id", "")),
            name = response.optString("name", draft.name),
        )
    }

    private fun request(method: String, path: String, body: String? = null): String {
        val endpoint = EndpointPolicy.normalize(settings.endpoint)
        val uri = URI.create(endpoint + path)
        val connection = uri.toURL().openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 8_000
            connection.readTimeout = 12_000
            connection.setRequestProperty("Accept", "application/json")

            if (settings.authMode == AuthMode.BEARER) {
                require(!token.isNullOrBlank()) { "Bearer token is not configured." }
                connection.setRequestProperty("Authorization", "Bearer $token")
            }

            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }

            val status = connection.responseCode
            val stream =
                if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (status !in 200..299) {
                val detail = runCatching {
                    JSONObject(response).optString("error")
                        .ifBlank { JSONObject(response).optString("message") }
                }.getOrNull()

                throw IOException(
                    "StarIntel HTTP $status" +
                        if (detail.isNullOrBlank()) "" else ": $detail",
                )
            }

            return response
        } finally {
            connection.disconnect()
        }
    }
}
