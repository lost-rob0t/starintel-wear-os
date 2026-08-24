package actor.starintel.wear.data

import android.content.Context
import actor.starintel.wear.model.SearchResult
import actor.starintel.wear.model.Settings
import actor.starintel.wear.model.Stats
import actor.starintel.wear.model.TargetDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StarIntelRepository(context: Context) {
    private val settingsStore = SettingsStore(context)
    private val statsCache = StatsCache(context)

    fun settings(): Settings = settingsStore.read()

    fun setEndpoint(endpoint: String) = settingsStore.setEndpoint(endpoint)

    fun setAuthMode(mode: actor.starintel.wear.model.AuthMode) =
        settingsStore.setAuthMode(mode)

    fun setToken(token: String) = settingsStore.setToken(token)

    fun cachedStats(): Stats = statsCache.read()

    suspend fun refreshStats(): Stats = withContext(Dispatchers.IO) {
        api().fetchStats().also(statsCache::write)
    }

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        api().search(query)
    }

    suspend fun createTarget(draft: TargetDraft) = withContext(Dispatchers.IO) {
        api().createTarget(draft)
    }

    private fun api(): StarIntelApi {
        val settings = settingsStore.read()
        return StarIntelApi(settings, settingsStore.token())
    }
}
