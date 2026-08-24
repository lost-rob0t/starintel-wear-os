package actor.starintel.wear.model

data class DTypeStat(
    val dtype: String,
    val count: Long,
)

data class Stats(
    val totalDocuments: Long = 0,
    val totalPeople: Long = 0,
    val byDType: List<DTypeStat> = emptyList(),
)

data class SearchResult(
    val id: String,
    val title: String,
    val dtype: String,
    val summary: String?,
    val url: String?,
)

data class TargetDraft(
    val name: String,
    val dtype: String = "person",
)

data class CreatedTarget(
    val id: String,
    val name: String,
)

enum class AuthMode {
    NONE,
    BEARER,
}

data class Settings(
    val endpoint: String,
    val authMode: AuthMode,
    val hasToken: Boolean,
)
