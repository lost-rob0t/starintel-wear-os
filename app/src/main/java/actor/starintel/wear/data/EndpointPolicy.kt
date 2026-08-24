package actor.starintel.wear.data

import java.net.URI

object EndpointPolicy {
    fun normalize(raw: String): String {
        val trimmed = raw.trim().trimEnd('/')
        require(trimmed.isNotEmpty()) { "Server endpoint is empty." }

        val uri = runCatching { URI.create(trimmed) }
            .getOrElse { throw IllegalArgumentException("Invalid server endpoint.") }

        require(uri.scheme == "http" || uri.scheme == "https") {
            "Server endpoint must use http:// or https://."
        }
        require(!uri.host.isNullOrBlank()) {
            "Server endpoint must include a host or IP address."
        }

        return trimmed
    }

    fun isCleartext(raw: String): Boolean =
        normalize(raw).startsWith("http://")
}
