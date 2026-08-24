package actor.starintel.wear.data

import android.content.Context
import actor.starintel.wear.model.AuthMode
import actor.starintel.wear.model.Settings

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("starintel.settings", Context.MODE_PRIVATE)
    private val secretStore = SecretStore(context)

    fun read(): Settings {
        val mode = runCatching {
            AuthMode.valueOf(prefs.getString(KEY_AUTH_MODE, AuthMode.NONE.name)!!)
        }.getOrDefault(AuthMode.NONE)

        return Settings(
            endpoint = prefs.getString(KEY_ENDPOINT, DEFAULT_ENDPOINT) ?: DEFAULT_ENDPOINT,
            authMode = mode,
            hasToken = secretStore.hasToken(),
        )
    }

    fun setEndpoint(endpoint: String) {
        prefs.edit().putString(KEY_ENDPOINT, EndpointPolicy.normalize(endpoint)).apply()
    }

    fun setAuthMode(mode: AuthMode) {
        prefs.edit().putString(KEY_AUTH_MODE, mode.name).apply()
    }

    fun setToken(token: String) = secretStore.putToken(token)

    fun token(): String? = secretStore.getToken()

    private companion object {
        const val KEY_ENDPOINT = "endpoint"
        const val KEY_AUTH_MODE = "auth_mode"

        // Change this in Settings on first launch.
        const val DEFAULT_ENDPOINT = "https://starintel.actor"
    }
}
