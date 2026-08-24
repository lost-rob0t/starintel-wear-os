package actor.starintel.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import actor.starintel.wear.data.StarIntelRepository
import actor.starintel.wear.model.AuthMode
import actor.starintel.wear.model.SearchResult
import actor.starintel.wear.model.Settings
import actor.starintel.wear.model.Stats
import actor.starintel.wear.model.TargetDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val stats: Stats = Stats(),
    val settings: Settings,
    val loading: Boolean = false,
    val message: String? = null,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val targetName: String = "",
    val targetDType: String = "person",
)

class StarIntelViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = StarIntelRepository(application)

    private val _state = MutableStateFlow(
        UiState(
            stats = repo.cachedStats(),
            settings = repo.settings(),
        ),
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchRequest {
            val stats = repo.refreshStats()
            _state.value = _state.value.copy(stats = stats)
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        _state.value = _state.value.copy(searchQuery = query)
        launchRequest {
            val results = repo.search(query)
            _state.value = _state.value.copy(searchResults = results)
        }
    }

    fun setTargetName(name: String) {
        _state.value = _state.value.copy(targetName = name)
    }

    fun setTargetDType(dtype: String) {
        _state.value = _state.value.copy(targetDType = dtype.ifBlank { "person" })
    }

    fun createTarget() {
        val state = _state.value
        if (state.targetName.isBlank()) {
            _state.value = state.copy(message = "Target name is required.")
            return
        }

        launchRequest {
            val target = repo.createTarget(
                TargetDraft(state.targetName.trim(), state.targetDType.trim()),
            )
            _state.value = _state.value.copy(
                targetName = "",
                message = "Created ${target.name}" +
                    if (target.id.isBlank()) "" else " · ${target.id}",
            )
        }
    }

    fun setEndpoint(endpoint: String) {
        runCatching {
            repo.setEndpoint(endpoint)
            _state.value = _state.value.copy(settings = repo.settings(), message = "Endpoint saved.")
        }.onFailure {
            _state.value = _state.value.copy(message = it.message)
        }
    }

    fun setAuthMode(mode: AuthMode) {
        repo.setAuthMode(mode)
        _state.value = _state.value.copy(settings = repo.settings())
    }

    fun setToken(token: String) {
        repo.setToken(token.trim())
        _state.value = _state.value.copy(settings = repo.settings(), message = "Token saved securely.")
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun launchRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            runCatching { block() }
                .onFailure {
                    _state.value = _state.value.copy(
                        message = it.message ?: it::class.simpleName,
                    )
                }
            _state.value = _state.value.copy(loading = false)
        }
    }
}
